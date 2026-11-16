package application;

//import Patient;

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import java.sql.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main extends Application {

  private static final String DB_URL = "jdbc:mysql://localhost:3306/hospitalAttempt1";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "class123"; // Change this
  private static int prevPatientID = 0;

  private final TextField patientID = new TextField();
  private final TextField nameField = new TextField();
  private final TextField ageField = new TextField();
  private final ComboBox<String> genderBox = new ComboBox<>();
  private final TextField phoneField = new TextField();
  private final TextField diagnoseField = new TextField();
  private final DatePicker dateField = new DatePicker();
  private final Label statusLabel = new Label("Enter patient details.");

  private final TextField patientSearch = new TextField();
  private final Button patientSearchButton = new Button();


  @Override

  public void start(Stage stage) {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(20));

    Label heading = new Label("Student Registration");     

    heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
    Label subtitle = new Label("College Student Services");
    VBox header = new VBox(5, heading, subtitle);
    header.setPadding(new Insets(0, 0, 20, 0));
    root.setTop(header);

    GridPane form = new GridPane();
    form.setHgap(12);
    form.setVgap(14);
    form.setPadding(new Insets(20));

    patientID.setText(String.format("%05d",prevPatientID));
    //patientID.setEditable(false);
    //patientID.setFocusTraversable(false);
    nameField.setPromptText("Full name");
    //nameField.setFocusTraversable(true);
    ageField.setPromptText("Age in years");
    phoneField.setPromptText("Phone number");
    genderBox.getItems().addAll("Female", "Male", "Non-binary", "Prefer not to say");
    genderBox.setPromptText("Select gender");
    diagnoseField.setPromptText("Reason for visit / Diagnosis");
    dateField.setPromptText("Admission date");

    addRow(form, 0, "Patient ID:", patientID);
    addRow(form, 1, "Patient name:", nameField);
    addRow(form, 2, "Age:", ageField);
    addRow(form, 3, "Gender:", genderBox);
    addRow(form, 4, "Phone number:", phoneField);
    addRow(form, 5, "Diagnosis/Reason for visit:", diagnoseField);
    addRow(form, 6, "Admission date:", dateField);

    
    
    
    ColumnConstraints labels = new ColumnConstraints(130);

    ColumnConstraints fields = new ColumnConstraints();

    fields.setHgrow(Priority.ALWAYS);

    form.getColumnConstraints().addAll(labels, fields);


    Button registerButton = new Button("Add");

    registerButton.setDefaultButton(true);

    registerButton.setOnAction(e -> addPatient());
    
    Button updateButton = new Button("Update");

    updateButton.setDefaultButton(true);

    //updateButton.setOnAction(e -> addPatient());
    
    Button deleteButton = new Button("Delete");

    deleteButton.setDefaultButton(true);


    Button clearButton = new Button("Clear");

    clearButton.setOnAction(e -> clearForm());
    
    GridPane database = new GridPane();
    //database.
    HBox CRUDbuttons = new HBox(4, registerButton, updateButton, deleteButton, clearButton);
    

    //HBox buttons = new HBox(10, registerButton, clearButton);

    //buttons.setAlignment(Pos.CENTER_RIGHT);


    VBox content = new VBox(12, form, CRUDbuttons, statusLabel);

    content.setMaxWidth(620);

    root.setCenter(content);

    root.setBottom(new Label("JavaFX • JDBC • MySQL"));
    
    
    TableColumn<Patient, String> patientIdCol = new TableColumn<>("ID");
    //patientIdCol.setCellValueFactory(cellData -> cellData.getValue().patientIDProperty());
    
    TableView<Patient> patientDatabase = new TableView<>();
    patientDatabase.getColumns().addAll(patientIdCol);

    //ObservableList<Patient> tableData - 

    stage.setTitle("Patient Registration System");

    stage.setScene(new Scene(root, 700, 520));

    stage.show();

  }

  private Patient[] getRowsByQuery(String sqlQuery) {
			  try (Connection connection =
		    	DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		    	Statement statement = connection.createStatement()) {
		    	ResultSet results = statement.executeQuery(sqlQuery);
		    	results.last();
		    	Patient[] returnList = new Patient[results.getRow()];
		    	//int listLength = results.getRow();
		    	results.beforeFirst();
		    	for(int i=0; i<returnList.length; i++) {
		    		results.next();
		    		returnList[i].patientId = results.getInt("patient_id");
		    		returnList[i].patientName = results.getString("name");
		    		returnList[i].patientAge = results.getInt("age");
		    		returnList[i].patientGender = results.getString("gender");
		    		returnList[i].patientPhone = results.getInt("phone");
		    		returnList[i].patientReason = results.getString("reason");
		    		returnList[i].patientDate = results.getDate("admission_date").toString();
		    		returnList[i].rowCreatedOn = results.getDate("created_at").toString();
		    	}//rowCreatedOn
		    	//(name, age, gender, phone, reason, admission_date)
		    	return returnList;
		    } catch (SQLException ex) {
		    	setStatus("Database error: " + ex.getMessage(), true);
		    	ex.printStackTrace();
		    	return null;
		    }
	  
  }
  private void addRow(GridPane grid, int row, String text, Control control) {

    Label label = new Label(text);

    label.setStyle("-fx-font-weight: bold;");

    control.setMaxWidth(Double.MAX_VALUE);

    grid.add(label, 0, row);

    grid.add(control, 1, row);

  }

  
  private boolean validateInformation(boolean idExistAlready) {
	  Pattern phonePattern = Pattern.compile("^[0-9]{3}([-\\s]?)[0-9]{3}\\1[0-9]{4}$");
	  Alert a = new Alert(AlertType.WARNING);
	  for(TextField field: new TextField[]{nameField, ageField, phoneField, diagnoseField}) {
		  if(field.getText().isBlank()) {
			  //System.out.println("blank field");
			  a.setContentText("cannot leave field blank");
			  setStatus("Cannot leave field blank.", true);
			  field.requestFocus();
			  return false;
		  }
	  }
	  if(genderBox.getValue()==null) {
		  setStatus("Gender not set.", true);
		  genderBox.requestFocus();
		  return false;
	  } else if (dateField.getValue()==null) {
		  setStatus("Date not set.", true);
		  dateField.requestFocus();
		  return false;
	  }
	  int patientInputtedAge;
	  try {
		  patientInputtedAge = Integer.parseInt(ageField.getText().trim());
	  } catch (NumberFormatException ex) {
		  setStatus("Age must be a whole number.", true);
		  //a.setContentText("age must be an integer");
		  ageField.requestFocus();
		  return false;
	  }
	  if(patientInputtedAge<0||patientInputtedAge>150) {
		  //System.out.println("age not 0-150");
		  setStatus("age must be in 0..150.", true);
		  //a.setContentText("age must be between 0-150");
		  ageField.requestFocus();
		  return false;
	  } else if(!phonePattern.matcher(phoneField.getText().trim()).matches()) {
		  //System.out.println("phone wrong");
		  setStatus("Phone format wrong.", true);
		  a.setContentText("accepted formats:\n#########\n###-###-###\n### ### ####");
		  a.show();
		  phoneField.requestFocus();
		  return false;
	  } else {
		  String[] selectedDate = Date.valueOf(dateField.getValue()).toString().split("-");
		  String[] currentDate = LocalDate.now().toString().split("-");
		  for(int i=0; i<3; i++) {
			  if(Integer.valueOf(selectedDate[i])>Integer.valueOf(currentDate[i])) {
				  a.setContentText("date must be either today or in the past");
				  dateField.requestFocus();
				  return false;
			  }
		  }
		  return true;
	  }
	  
  }
  
  private void updatePatient(int patientIdToUpdate) {
	  System.out.println("update RUN");
	  if(!validateInformation(false)) { return; }
	  if(!validateInformation(false)) { return; }
	  String patientName = nameField.getText().trim();
	  String patientPhone = phoneField.getText().trim();
	  String patientReason = diagnoseField.getText().trim();
	  String patientGender = genderBox.getValue();
	  Date patientDate = Date.valueOf(dateField.getValue());
	  int patientAge = Integer.parseInt(ageField.getText().trim());
	  patientPhone = String.join("",patientPhone.split("[-\\s]"));
	  if(patientGender=="Prefer not to say") {patientGender = null;}
	  String sql = "INSERT INTO PatientInfo "
		        + "(name, age, gender, phone, reason, admission_date) "
		        + "VALUES (?, ?, ?, ?, ?, ?)";
		    try (Connection connection =
		    	DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		    	PreparedStatement statement = connection.prepareStatement(sql)) {
		    	statement.setString(1, patientName);
		    	statement.setInt(2, patientAge);
		    	statement.setString(3, patientGender);
		    	statement.setString(4, patientPhone);
		    	statement.setString(5, patientReason);
		    	statement.setDate(6, patientDate);
		    	
		    	int count = statement.executeUpdate();
		    	if (count == 1) {
		    		setStatus("Patient registered successfully!", false);
		    		clearFields();
		    	} else {
		    		setStatus("No record was inserted.", true);
		    	}
		    } catch (SQLException ex) {
		    	setStatus("Database error: " + ex.getMessage(), true);
		    	ex.printStackTrace();
		    }
  }

  private void addPatient() {
	  System.out.println("register RUN");
	  if(!validateInformation(false)) { return; }
	  String patientName = nameField.getText().trim();
	  String patientPhone = phoneField.getText().trim();
	  String patientReason = diagnoseField.getText().trim();
	  String patientGender = genderBox.getValue();
	  Date patientDate = Date.valueOf(dateField.getValue());
	  int patientAge = Integer.parseInt(ageField.getText().trim());
	  patientPhone = String.join("",patientPhone.split("[-\\s]"));
	  if(patientGender=="Prefer not to say") {patientGender = null;}
	  
	  //
    String sql = "INSERT INTO PatientInfo "
        + "(name, age, gender, phone, reason, admission_date) "
        + "VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection connection =
    	DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    	PreparedStatement statement = connection.prepareStatement(sql)) {
    	statement.setString(1, patientName);
    	statement.setInt(2, patientAge);
    	statement.setString(3, patientGender);
    	statement.setString(4, patientPhone);
    	statement.setString(5, patientReason);
    	statement.setDate(6, patientDate);
    	
    	int count = statement.executeUpdate();
    	if (count == 1) {
    		setStatus("Patient registered successfully!", false);
    		clearFields();
    	} else {
    		setStatus("No record was inserted.", true);
    	}
    } catch (SQLException ex) {
    	setStatus("Database error: " + ex.getMessage(), true);
    	ex.printStackTrace();
    }
}


  private void clearForm() {

    clearFields();

    setStatus("Form cleared.", false);

  }


  private void clearFields() {
	  for(TextField field: new TextField[]{nameField, ageField, phoneField, diagnoseField}) {
		  field.clear();
	  }
	  genderBox.getSelectionModel().clearSelection();
	  dateField.setValue(null);
  }


  private void setStatus(String message, boolean error) {

    statusLabel.setText(message);

    statusLabel.setStyle(error ? "-fx-text-fill: #b00020;"

                  : "-fx-text-fill: #167545;");

  }


  public static void main(String[] args) {

    launch(args);

  }

}

