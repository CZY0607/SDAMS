SDAMS - Member 2: Administrator + User Management
Package: com.mycompany.assignment  (copy the .java files into your NetBeans project's source package)

Run alone to test:  AdministratorFrame.main()  -> logs in as seeded AD001
First run creates data/users.txt with:
  MG001 / Manager123     AD001 / Admin1234      (change before demo)

users.txt   role|userID|name|contact|passwordHash|extra     (extra: enrollment status, or ;-separated specializations)
payments.txt        paymentID|registrationID|amount|paymentDate|paymentMethod|status      (Member 3 writes, we only read)
registrations.txt   registrationID|studentID|classID|bookingDate|status                   (Member 3 writes, we only read)

Login (Member 1):   User u = userService.findById(id);  if (u != null && u.verifyPassword(pwd)) ...
Open admin screen:  AdministratorFrame f = new AdministratorFrame(userService, u); f.setOnLogout(...); f.setVisible(true);
Staff report:       userService.generateStaffReport(role, name)
Java 8+ compatible.
