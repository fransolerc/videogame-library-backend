# Usa una imagen base con Maven y JDK para la etapa de construcción
FROM maven:3.9-eclipse-temurin-25 AS builder

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo pom.xml para descargar las dependencias
COPY pom.xml .

# Descarga las dependencias de Maven
RUN mvn dependency:go-offline

# Copia el resto del código fuente
COPY src ./src

# Compila la aplicación, omitiendo los tests para acelerar el proceso
RUN mvn package -DskipTests

# Usa una imagen base más ligera para la etapa de ejecución
FROM eclipse-temurin:25-jre-jammy

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR desde la etapa de construcción
COPY --from=builder /app/target/videogame-library-backend-0.0.1-SNAPSHOT.jar .

# Expone el puerto en el que se ejecuta la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "videogame-library-backend-0.0.1-SNAPSHOT.jar"]
