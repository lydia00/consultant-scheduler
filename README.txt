**********************
 Consultant Scheduler
**********************
A GUI-based scheduling application to manage customer records and appointments.

Author: Lydia Husser
Version: 1.2
31 July, 2021

Created with IntelliJ IDEA 2021.1.3 (Community Edition)
JDK version 11.0.11
JavaFX version 11.0.2
MySQL Connector mysql-connector-java-8.0.22

------------
Instructions
------------

Running the application
-----------------------
1. Open the IntelliJ IDE.
2. Click File > Open.
3. Navigate to the root folder for the application (consultant-scheduler_lhusser) and click OK.
4. If this is your first time running the application, click Edit Configurations on the upper right of the screen and paste the following in the VM Options field.

--module-path ${PATH_TO_FX} --add-modules javafx.fxml,javafx.controls,javafx.graphics

5. Click the green arrow on the upper right of the screen to run the application.
6. Make sure you have the JDK version, JavaFX version, and MySQL Connector driver (see the top of this document for compatible versions).

Logging in
----------
1. On the Login screen, enter your username and password and click "Login." A message will display for invalid credentials.
2. If you have an appointment within the next 15 minutes, an alert will display. Click OK.
3. This directs you to the Appointments Screen.

Appointments screen
-------------------
From the Appointments screen, you can view, add, modify, and cancel an appointment. You can also view appointments by month or week.
For customer records, click "Go to Customer Records". To generate a report, click "Generate Reports".

Customer Records screen
-----------------------
From the Customer Records screen, you can view, add, modify and delete a customer.
Note: If you attempt to delete a customer with associated appointments, the application will alert you that it must also delete the appointments before it can delete the customer. Click OK to delete both the appointments and the customer.
For appointments, click "Go to Appointments". To generate a report, click "Generate Reports".

Generate Reports screen
-----------------------
Click a report to generate from the options presented on the Generate Reports screen. The report will generate in the text field on the screen.
For appointments, click "Go to Appointments". For customer records, click "Go to Customer Records".
