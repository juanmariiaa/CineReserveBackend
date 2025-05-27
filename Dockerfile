# Usamos la imagen oficial más reciente de Eclipse Temurin (antes OpenJDK)
FROM eclipse-temurin:21-jdk-jammy AS build

# Directorio de trabajo
WORKDIR /app

# Copia los archivos de proyecto (suponiendo que usas Maven)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Construye el proyecto y omite los tests
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Segunda etapa: imagen runtime más ligera
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copia el JAR construido desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]