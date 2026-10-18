/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author elabe
 */


import model.*;
import service.DatabaseService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import java.sql.*;
import java.util.*;

public class DashboardController {
    
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblOpenJobs;
    @FXML private Label lblTotalCandidates;
    @FXML private Label lblAcceptanceRate;
    
    @FXML private PieChart employeesByDeptChart;
    @FXML private LineChart<String, Number> candidaturesChart;
    
    
    @FXML
    public void initialize() {        
        // Charger les données
        loadStatistics();
        loadCharts();
    }
    
    /**
     * Charge les statistiques principales
     */
    private void loadStatistics() {
        try (Connection conn = DatabaseService.getConnection()) {
            
            // Total employés
            String sqlEmployees = "SELECT COUNT(*) FROM employee";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlEmployees)) {
                if (rs.next()) {
                    lblTotalEmployees.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Offres ouvertes
            String sqlJobs = "SELECT COUNT(*) FROM job WHERE statut = 'Ouverte'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlJobs)) {
                if (rs.next()) {
                    lblOpenJobs.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Total candidats
            String sqlCandidates = "SELECT COUNT(*) FROM candidate";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCandidates)) {
                if (rs.next()) {
                    lblTotalCandidates.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Taux d'acceptation
            String sqlAcceptance = "SELECT COUNT(*) * 100.0 / (SELECT COUNT(*) FROM application) " +
                                  "FROM application WHERE recommandation = 'Accepté'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlAcceptance)) {
                if (rs.next()) {
                    double rate = rs.getDouble(1);
                    lblAcceptanceRate.setText(String.format("%.1f%%", rate));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Valeurs par défaut en cas d'erreur
            lblTotalEmployees.setText("0");
            lblOpenJobs.setText("0");
            lblTotalCandidates.setText("0");
            lblAcceptanceRate.setText("0%");
        }
    }
    
    /**
     * Charge les graphiques
     */
    private void loadCharts() {
        loadEmployeesByDepartmentChart();
        loadCandidaturesChart();
    }
    
    /**
     * Graphique: Employés par département
     */
    private void loadEmployeesByDepartmentChart() {
        //Création de la liste des données
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        //Connexion à la base de données
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT departement, COUNT(*) as count FROM employee " +
                        "GROUP BY departement ORDER BY count DESC";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    String dept = rs.getString("departement");
                    int count = rs.getInt("count");
                    pieData.add(new PieChart.Data(dept, count));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Données de démonstration
            pieData.addAll(
                new PieChart.Data("IT", 25),
                new PieChart.Data("RH", 10),
                new PieChart.Data("Finance", 15),
                new PieChart.Data("Marketing", 12)
            );
        }
        //Affichage du graphique
        employeesByDeptChart.setData(pieData);
        employeesByDeptChart.setLegendVisible(true);
    }
    
    /**
     * Graphique: Candidatures par mois
     */
    private void loadCandidaturesChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Candidatures");
        
        try (Connection conn = DatabaseService.getConnection()) {
            /*compter le nombre de candidatures enregistrées chaque mois durant les 6 derniers mois 
            et d’afficher les résultats triés par ordre chronologique*/
            String sql = "SELECT DATE_FORMAT(date_candidature, '%Y-%m') as mois, COUNT(*) as count " +
                        "FROM application " +
                        "WHERE date_candidature >= DATE_SUB(NOW(), INTERVAL 6MONTH) " +
                        "GROUP BY mois ORDER BY mois";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    String mois = rs.getString("mois");
                    int count = rs.getInt("count");
                    series.getData().add(new XYChart.Data<>(mois, count));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Données de démonstration
            series.getData().addAll(
                new XYChart.Data<>("2025-11", 15),
                new XYChart.Data<>("2025-12", 22),
                new XYChart.Data<>("2026-01", 18),
                new XYChart.Data<>("2026-02", 30),
                new XYChart.Data<>("2026-03", 25),
                new XYChart.Data<>("2026-04", 28)
            );
        }
        
        candidaturesChart.getData().clear();
        candidaturesChart.getData().add(series);
    }    
    /**
     * Rafraîchir les données
     */
    public void refresh() {
        loadStatistics();
        loadCharts();
    }
}