package com.clinicapp.model;

public class Prescription {

    private String medicineId;
    private int quantity;     // number of units prescribed
    private String dosage;    // e.g., "1 tablet twice a day"

    public Prescription() {}

    public Prescription(String medicineId, int quantity, String dosage) {
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.dosage = dosage;
    }

    // Getters and Setters
    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
}
