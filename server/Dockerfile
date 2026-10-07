# Dockerfile para Servidor Backend Ktor (Render.com)
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app

# Copiar archivos del proyecto
COPY build.gradle.kts settings.gradle.kts ./
COPY src/ src/

# Construir la distribucion
RUN gradle installDist --no-daemon

# Imagen final de ejecucion
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar ejecutable compilado
COPY --from=build /app/build/install/ticket-server /app/

# Puerto expuesto
EXPOSE 8080

# Comando para iniciar el servidor Ktor
CMD ["/app/bin/ticket-server"]
