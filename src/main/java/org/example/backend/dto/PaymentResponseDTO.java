package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String url;
    private String error;

    public PaymentResponseDTO(String url) {
        this.url = url;
        this.error = null;
    }
}