package com.example.be.domain.repository;

public interface ContractFileStorage {
    String store(byte[] fileData, String fileName);
    byte[] load(String filePath);
}
