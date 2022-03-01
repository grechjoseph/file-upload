FROM openjdk

ENV PORT 8080
ENV CLASSPATH /opt/lib

COPY target/large-file-upload-0.0.1-SNAPSHOT.jar /file-upload.jar

CMD ["sh", "-c", "java $APPLICATION_ARGS -jar /file-upload.jar"]