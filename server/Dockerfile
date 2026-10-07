# Dockerfile para Servidor Backend Ktor (TicketApp / NETSUR)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiar archivos de construccion de Gradle
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./
COPY src/ src/

# Dar permisos de ejecucion y construir la distribucion
RUN chmod +x gradlew
RUN ./gradlew installDist --no-daemon

# Imagen final de ejecucion
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar la aplicacion construida
COPY --from=build /app/build/install/ticket-server /app/

# Puerto expuesto
EXPOSE 8080

# Comando para iniciar el servidor Ktor
CMD ["/app/bin/ticket-server"]
