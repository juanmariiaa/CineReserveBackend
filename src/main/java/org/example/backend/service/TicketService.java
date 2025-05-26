package org.example.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.example.backend.model.Movie;
import org.example.backend.model.Reservation;
import org.example.backend.model.Screening;
import org.example.backend.model.SeatReservation;
import org.example.backend.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final ReservationRepository reservationRepository;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Genera un PDF con el ticket de la reserva y lo envía por correo electrónico
     *
     * @param reservationId ID de la reserva
     */
    public void generateAndSendTicket(Long reservationId) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + reservationId));

            byte[] pdfTicket = generateTicketPdf(reservation);
            
            String userEmail = reservation.getUser().getEmail();
            String movieTitle = reservation.getScreening().getMovie().getTitle();
            
            String subject = "Tu entrada para " + movieTitle;
            String body = "<html><body>" +
                    "<h2>¡Gracias por tu compra en CineReserve!</h2>" +
                    "<p>Adjunto encontrarás tu entrada para <strong>" + movieTitle + "</strong>.</p>" +
                    "<p>Recuerda presentar este documento (impreso o en tu dispositivo) en la entrada del cine.</p>" +
                    "<p>¡Esperamos que disfrutes de la película!</p>" +
                    "<p>Saludos,<br>El equipo de CineReserve</p>" +
                    "</body></html>";
            
            emailService.sendEmailWithAttachment(
                    userEmail,
                    subject,
                    body,
                    pdfTicket,
                    "entrada_" + reservation.getId() + ".pdf"
            );
            
            log.info("Ticket generado y enviado para la reserva ID: {}", reservationId);
        } catch (Exception e) {
            log.error("Error al generar y enviar ticket para la reserva ID {}: {}", reservationId, e.getMessage(), e);
            throw new RuntimeException("Error al generar y enviar ticket", e);
        }
    }

    /**
     * Genera un PDF con el ticket de la reserva
     *
     * @param reservation La reserva
     * @return Arreglo de bytes con el PDF generado
     */
    private byte[] generateTicketPdf(Reservation reservation) throws DocumentException, IOException, WriterException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Obtener datos necesarios
        Screening screening = reservation.getScreening();
        Movie movie = screening.getMovie();
        String movieTitle = movie.getTitle();
        String roomName = "Sala " + screening.getRoom().getNumber();
        String screeningDate = screening.getStartTime().format(DATE_FORMATTER);
        String screeningTime = screening.getStartTime().format(TIME_FORMATTER);
        String userName = reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName();
        String reservationId = reservation.getId().toString();
        
        List<String> seatLabels = reservation.getSeatReservations().stream()
                .map(sr -> sr.getSeat().getRowLabel() + sr.getSeat().getColumnNumber())
                .collect(Collectors.toList());
        String seats = String.join(", ", seatLabels);
        
        // Configurar fuentes
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
        
        // Crear contenido
        Paragraph title = new Paragraph("ENTRADA DE CINE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Generar código QR
        byte[] qrCodeImage = generateQRCode(reservationId, 200, 200);
        Image qrCode = Image.getInstance(qrCodeImage);
        qrCode.setAlignment(Element.ALIGN_CENTER);
        document.add(qrCode);
        
        Paragraph qrInfo = new Paragraph("ID de Reserva: " + reservationId, smallFont);
        qrInfo.setAlignment(Element.ALIGN_CENTER);
        qrInfo.setSpacingAfter(20);
        document.add(qrInfo);
        
        // Crear tabla con datos de la película
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20);
        table.setSpacingAfter(20);
        
        // Estilos de celdas
        PdfPCell cellHeader = new PdfPCell();
        cellHeader.setBackgroundColor(new BaseColor(240, 240, 240));
        cellHeader.setPadding(8);
        
        PdfPCell cellContent = new PdfPCell();
        cellContent.setPadding(8);
        
        // Película
        cellHeader.setPhrase(new Phrase("Película", headerFont));
        table.addCell(cellHeader);
        cellContent.setPhrase(new Phrase(movieTitle, normalFont));
        table.addCell(cellContent);
        
        // Sala
        cellHeader.setPhrase(new Phrase("Sala", headerFont));
        table.addCell(cellHeader);
        cellContent.setPhrase(new Phrase(roomName, normalFont));
        table.addCell(cellContent);
        
        // Fecha
        cellHeader.setPhrase(new Phrase("Fecha", headerFont));
        table.addCell(cellHeader);
        cellContent.setPhrase(new Phrase(screeningDate, normalFont));
        table.addCell(cellContent);
        
        // Hora
        cellHeader.setPhrase(new Phrase("Hora", headerFont));
        table.addCell(cellHeader);
        cellContent.setPhrase(new Phrase(screeningTime, normalFont));
        table.addCell(cellContent);
        
        // Asientos
        cellHeader.setPhrase(new Phrase("Asientos", headerFont));
        table.addCell(cellHeader);
        cellContent.setPhrase(new Phrase(seats, normalFont));
        table.addCell(cellContent);
        
        // Cliente
        cellHeader.setPhrase(new Phrase("Cliente", headerFont));
        table.addCell(cellHeader);
        cellContent.setPhrase(new Phrase(userName, normalFont));
        table.addCell(cellContent);
        
        document.add(table);
        
        // Pie de página
        Paragraph footer = new Paragraph("Esta entrada es válida solo para la función indicada. " +
                "Por favor, preséntala en la entrada del cine 15 minutos antes del inicio de la película.", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        document.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * Genera un código QR con la información proporcionada
     *
     * @param data   Datos a codificar en el QR
     * @param width  Ancho del QR
     * @param height Alto del QR
     * @return Imagen del código QR en formato de bytes
     */
    private byte[] generateQRCode(String data, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        return outputStream.toByteArray();
    }
} 