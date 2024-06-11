# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Compile the application
RUN mvn clean install

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-cp", "target/RDF-Kad-1.0-SNAPSHOT.jar", "org.rdfkad.Node"]

# The CMD will be used to pass arguments to the ENTRYPOINT
CMD ["0.0.0.0", "8080"]
