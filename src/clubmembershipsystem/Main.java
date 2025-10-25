package clubmembershipsystem;

import Config.Config;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Config con = new Config();
        con.connectDB();
        Scanner sc = new Scanner(System.in);
        char cont;

        do {
            System.out.println("CLUB MEMBERSHIP\n");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("\nEnter choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    int attempts = 0;
                    Map<String, Object> user = null;

                    while (attempts < 3) {
                        System.out.print("\nEnter Email: ");
                        String email = sc.next();
                        System.out.print("Enter Password: ");
                        String pass = sc.next();

                        String hashPass = con.hashPassword(pass);
                        String loginQry = "SELECT * FROM users_tbl WHERE email = ? AND pass = ?";
                        List<Map<String, Object>> users = con.fetchRecords(loginQry, email, hashPass);

                        if (users.isEmpty()) {
                            attempts++;
                            System.out.println("INVALID CREDENTIALS (" + attempts + "/3)");
                            if (attempts == 3) {
                                System.out.println("Too many failed attempts. Returning to main menu...");
                                break;
                            }
                        } else {
                            user = users.get(0);
                            String status = user.get("stat").toString();

                            if (!status.equalsIgnoreCase("approved")) {
                                System.out.println("Your account has not been approved by the SAO yet.");
                                user = null;
                                break;
                            }
                            attempts = 3;
                        }
                    }

                    if (user == null) {
                        break;
                    }

                    int loggedId = Integer.parseInt(user.get("u_id").toString());
                    String checkOfficer = "SELECT p.pos_name FROM membership_tbl m JOIN position_tbl p ON m.pos_id=p.pos_id WHERE m.u_id=?";
                    List<Map<String, Object>> officerPos = con.fetchRecords(checkOfficer, loggedId);

                    if (!officerPos.isEmpty()) {
                        String posName = officerPos.get(0).get("pos_name").toString().toLowerCase();
                        if (posName.equals("president") || posName.equals("vice president") ||
                            posName.equals("secretary") || posName.equals("treasurer")) {
                            con.updateRecord("UPDATE users_tbl SET role='CLUB OFFICER' WHERE u_id=?", loggedId);
                            user.put("role", "CLUB OFFICER");
                        }
                    }

                    String role = user.get("role").toString();
                    System.out.println("\nLOGIN SUCCESS!");

                    if (role.equals("SAO")) {
                        SAO sao = new SAO();
                        sao.SAODashboard();
                        break;
                    }

                    if (role.equals("CLUB OFFICER")) {
                        ClubOfficer officer = new ClubOfficer(user);
                        officer.officerDashboard();
                        break;
                    }

                    if (role.equals("STUDENT")) {
                        Student student = new Student(user);
                        student.studentDashboard();
                        break;
                    }

                case 2:
                    String utp = null, newEmail;

                    System.out.print("\nEnter user name: ");
                    String name = sc.next();

                    while (true) {
                        System.out.print("Enter user email: ");
                        newEmail = sc.next();

                        String emailCheckQry = "SELECT * FROM users_tbl WHERE email=?";
                        List<Map<String, Object>> existing = con.fetchRecords(emailCheckQry, newEmail);

                        if (!existing.isEmpty()) {
                            Map<String, Object> existUser = existing.get(0);
                            if (existUser.get("role").equals("STUDENT") && utp.equals("CLUB OFFICER")) {
                                con.updateRecord("UPDATE users_tbl SET role=? WHERE email=?", utp, newEmail);
                                System.out.println("Existing student upgraded to CLUB OFFICER!");
                            } else {
                                System.out.println("Email already exists. Use another email.");
                            }
                        } else {
                            break;
                        }
                    }

                    System.out.print("Enter user Type (1 - SAO / 2 - CLUB OFFICER / 3 - STUDENT): ");
                    int usertp = sc.nextInt();

                    if (usertp == 1) {
                        utp = "SAO";
                    } else if (usertp == 2) {
                        utp = "CLUB OFFICER";
                    } else {
                        utp = "STUDENT";
                    }

                    System.out.print("Enter Password: ");
                    String p = sc.next();

                    String hashPass = con.hashPassword(p);

                    String dept;
                    int yr;

                    if (utp.equals("CLUB OFFICER") || utp.equals("STUDENT")) {
                        System.out.print("Enter Department: ");
                        dept = sc.next();
                        System.out.print("Enter Year Level: ");
                        yr = sc.nextInt();
                    } else {
                        dept = "N/A";
                        yr = 0;
                    }

                    con.addRecord(
                        "INSERT INTO users_tbl(name, email, pass, role, department, year_lvl, stat) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        name, newEmail, hashPass, utp, dept, yr, "Pending"
                    );

                    System.out.println("Account approval is pending...");
                    break;

                case 3:
                    System.out.println("Thank you! Exiting system.");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }

            System.out.print("\nDo you want to continue? (Y/N): \n");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');

        System.out.println("Program Ended.");
    }
}