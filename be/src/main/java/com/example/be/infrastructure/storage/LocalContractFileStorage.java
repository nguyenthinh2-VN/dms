package com.example.be.infrastructure.storage;

import com.example.be.domain.repository.ContractFileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

@Component
public class LocalContractFileStorage implements ContractFileStorage {

    private final Path storageLocation;

    public LocalContractFileStorage(@Value("${app.contract.storage-path}") String storagePath) {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String store(byte[] fileData, String fileName) {
        if (fileName.contains("..")) {
            throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
        }
        try {
            Path targetLocation = this.storageLocation.resolve(fileName).normalize();
            if (!targetLocation.startsWith(this.storageLocation)) {
                 throw new RuntimeException("Cannot store file outside of storage directory.");
            }
            Files.createDirectories(targetLocation.getParent());
            Files.write(targetLocation, fileData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public byte[] load(String filePath) {
        if (filePath.contains("..")) {
            throw new RuntimeException("Sorry! Filepath contains invalid path sequence " + filePath);
        }
        try {
            Path fileLocation = this.storageLocation.resolve(filePath).normalize();
            if (!fileLocation.startsWith(this.storageLocation)) {
                 throw new RuntimeException("Cannot load file outside of storage directory.");
            }
            return Files.readAllBytes(fileLocation);
        } catch (IOException ex) {
            throw new RuntimeException("File not found " + filePath, ex);
        }
    }
}
