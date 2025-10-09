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
                    System.out.print("\nEnter Email: ");
                    String email = sc.next();
                    System.out.print("Enter Password: ");
                    String pass = sc.next();

                    String loginQry = "SELECT * FROM users_tbl WHERE email = ? AND pass = ?";
                    List<Map<String, Object>> users = con.fetchRecords(loginQry, email, pass);

                    if (users.isEmpty()) {
                        System.out.println("INVALID CREDENTIALS");
                        break;
                    }

                    Map<String, Object> user = users.get(0);
                    String role = user.get("role").toString();
                    System.out.println("\nLOGIN SUCCESS!");

                    if (role.equals("SAO")) {
                        do {
                            System.out.println("\nWELCOME TO SAO DASHBOARD");
                            System.out.println("1. Approve Application");
                            System.out.println("2. View Clubs");
                            System.out.println("3. Exit");
                            System.out.print("\nEnter your choice: ");
                            int saoChoice = sc.nextInt();

                            switch (saoChoice) {
                                case 1:
                                    String pendingQry = "SELECT * FROM clubs_tbl WHERE status = 'Pending'";
                                    List<Map<String, Object>> pending = con.fetchRecords(pendingQry);

                                    if (pending.isEmpty()) {
                                        System.out.println("No pending applications.");
                                    } else {
                                        con.viewRecords(
                                                pendingQry,
                                                new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                                new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                        );

                                        System.out.print("Enter Club ID to approve: ");
                                        int approveId = sc.nextInt();
                                        con.updateRecord("UPDATE clubs_tbl SET status='Approved' WHERE c_id=?", approveId);
                                        System.out.println("Club approved successfully!");
                                    }
                                    break;

                                case 2:
                                    con.viewRecords(
                                            "SELECT * FROM clubs_tbl",
                                            new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                            new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                    );
                                    break;

                                case 3:
                                    break;

                                default:
                                    System.out.println("Invalid choice.");
                            }

                            System.out.print("Continue as SAO? (Y/N): ");
                            cont = sc.next().charAt(0);

                        } while (cont == 'Y' || cont == 'y');
                        break;
                    }

                    if (role.equals("AVP")) {
                        do {
                            System.out.println("\nWELCOME TO AVP DASHBOARD");
                            System.out.println("1. Approve Application");
                            System.out.println("2. View Clubs");
                            System.out.println("3. Exit");
                            System.out.print("\nEnter your choice: ");
                            int avpChoice = sc.nextInt();

                            switch (avpChoice) {
                                case 1:
                                    String approvedQry = "SELECT * FROM clubs_tbl WHERE status='Approved'";
                                    List<Map<String, Object>> approved = con.fetchRecords(approvedQry);

                                    if (approved.isEmpty()) {
                                        System.out.println("No clubs pending establishment.");
                                    } else {
                                        con.viewRecords(
                                                approvedQry,
                                                new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                                new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                        );

                                        System.out.print("Enter Club ID to establish: ");
                                        int clubId = sc.nextInt();

                                        con.updateRecord("UPDATE clubs_tbl SET status='Established' WHERE c_id=?", clubId);

                                        con.updateRecord(
                                                "INSERT INTO membership_tbl (u_id, c_id, position, join_date) " +
                                                "SELECT created_by, c_id, 'Officer', CURRENT_DATE FROM clubs_tbl WHERE c_id=?",
                                                clubId
                                        );

                                        con.updateRecord(
                                                "UPDATE users_tbl SET role='CLUB OFFICER' " +
                                                "WHERE u_id=(SELECT created_by FROM clubs_tbl WHERE c_id=?)",
                                                clubId
                                        );

                                        System.out.println("Club successfully established!");
                                    }
                                    break;

                                case 2:
                                    con.viewRecords(
                                            "SELECT * FROM clubs_tbl",
                                            new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                            new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                    );
                                    break;

                                case 3:
                                    break;

                                default:
                                    System.out.println("Invalid choice.");
                            }

                            System.out.print("Continue as AVP? (Y/N): ");
                            cont = sc.next().charAt(0);

                        } while (cont == 'Y' || cont == 'y');
                        break;
                    }

                    if (role.equals("CLUB OFFICER")) {
                        String officerEmail = user.get("email").toString();

                        do {
                            System.out.println("\nWELCOME TO OFFICER DASHBOARD");
                            System.out.println("1. Manage Members");
                            System.out.println("2. Exit");
                            System.out.print("\nEnter your choice: ");
                            int officerChoice = sc.nextInt();

                            switch (officerChoice) {
                                case 1:
                                    String clubQry =
                                            "SELECT DISTINCT c.c_id, c.c_name FROM clubs_tbl c " +
                                            "JOIN users_tbl u ON c.created_by = u.u_id " +
                                            "LEFT JOIN membership_tbl m ON c.c_id = m.c_id " +
                                            "WHERE u.email = ? OR m.u_id IN (SELECT u_id FROM users_tbl WHERE email = ?)";
                                    List<Map<String, Object>> clubs = con.fetchRecords(clubQry, officerEmail, officerEmail);

                                    if (clubs.isEmpty()) {
                                        System.out.println("You do not manage any clubs.");
                                        break;
                                    }

                                    Map<String, Object> club = clubs.get(0);
                                    int clubId = Integer.parseInt(club.get("c_id").toString());
                                    String clubName = club.get("c_name").toString();

                                    System.out.println("\nManaging Club: " + clubName);
                                    System.out.println("1. Add Member");
                                    System.out.println("2. Edit Member Position");
                                    System.out.println("3. Remove Member");
                                    System.out.print("\nChoose option: ");
                                    int manageChoice = sc.nextInt();
                                    sc.nextLine();

                                    String memberQry =
                                            "SELECT m.mem_id, u.name, u.email, m.position FROM membership_tbl m " +
                                            "JOIN users_tbl u ON m.u_id = u.u_id WHERE m.c_id = ?";
                                    List<Map<String, Object>> members = con.fetchRecords(memberQry, clubId);

                                    System.out.println("\n--- Club Members ---");
                                    if (members.isEmpty()) {
                                        System.out.println("No members yet.");
                                    } else {
                                        for (Map<String, Object> m : members) {
                                            System.out.println(
                                                    "ID: " + m.get("mem_id") +
                                                    " | Name: " + m.get("name") +
                                                    " | Email: " + m.get("email") +
                                                    " | Position: " + m.get("position")
                                            );
                                        }
                                    }

                                    switch (manageChoice) {
                                        case 1:
                                            System.out.print("\nEnter User ID of new member: ");
                                            int userId = sc.nextInt();
                                            sc.nextLine();
                                            System.out.print("Enter Position (Member/Officer): ");
                                            String position = sc.nextLine();

                                            List<Map<String, Object>> checkDup =
                                                    con.fetchRecords("SELECT * FROM membership_tbl WHERE u_id=? AND c_id=?", userId, clubId);

                                            if (!checkDup.isEmpty()) {
                                                System.out.println("User already a member.");
                                                break;
                                            }

                                            con.updateRecord(
                                                    "INSERT INTO membership_tbl (u_id, c_id, position, join_date) VALUES (?, ?, ?, CURRENT_DATE)",
                                                    userId, clubId, position
                                            );
                                            System.out.println("Member added successfully!");
                                            break;

                                        case 2:
                                            System.out.print("\nEnter Membership ID to edit: ");
                                            int memEdit = sc.nextInt();
                                            sc.nextLine();
                                            System.out.print("Enter New Position (Member/Officer): ");
                                            String newPos = sc.nextLine();
                                            con.updateRecord("UPDATE membership_tbl SET position=? WHERE mem_id=?", newPos, memEdit);
                                            System.out.println("Position updated successfully!");
                                            break;

                                        case 3:
                                            System.out.print("\nEnter Membership ID to remove: ");
                                            int memRemove = sc.nextInt();
                                            con.updateRecord("DELETE FROM membership_tbl WHERE mem_id=?", memRemove);
                                            System.out.println("Member removed successfully!");
                                            break;

                                        default:
                                            System.out.println("Invalid option.");
                                    }
                                    break;

                                case 2:
                                    return;

                                default:
                                    System.out.println("Invalid choice.");
                            }

                            System.out.print("Continue as Officer? (Y/N): ");
                            cont = sc.next().charAt(0);

                        } while (cont == 'Y' || cont == 'y');
                        break;
                    }

                    if (role.equals("STUDENT")) {
                        do {
                            System.out.println("\nWELCOME TO STUDENT DASHBOARD");
                            System.out.println("1. Create Club");
                            System.out.println("2. Join Club");
                            System.out.println("3. View Clubs");
                            System.out.println("4. Update Club Application");
                            System.out.println("5. Remove Club Application");
                            System.out.println("6. Exit");
                            System.out.print("\nEnter your choice: ");
                            int sChoice = sc.nextInt();

                            switch (sChoice) {
                                case 1:
                                    sc.nextLine();
                                    System.out.print("\nEnter Club Name: ");
                                    String cName = sc.nextLine();
                                    System.out.print("Enter Club Type: ");
                                    String cType = sc.nextLine();
                                    System.out.print("Enter Department: ");
                                    String dep = sc.nextLine();
                                    System.out.print("Enter Application Date (YYYY-MM-DD): ");
                                    String appDate = sc.nextLine();
                                    int sId = Integer.parseInt(user.get("u_id").toString());

                                    con.updateRecord(
                                            "INSERT INTO clubs_tbl (c_name, c_type, dep_club, status, created_by, created_date) VALUES (?, ?, ?, ?, ?, ?)",
                                            cName, cType, dep, "Pending", sId, appDate
                                    );
                                    System.out.println("Club creation request submitted.");
                                    break;

                                case 2:
                                    con.viewRecords(
                                            "SELECT * FROM clubs_tbl WHERE status='Established'",
                                            new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                            new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                    );

                                    System.out.print("\nEnter Club ID to join: ");
                                    int cJoin = sc.nextInt();
                                    int sJoin = Integer.parseInt(user.get("u_id").toString());

                                    List<Map<String, Object>> exist = con.fetchRecords(
                                            "SELECT * FROM membership_tbl WHERE u_id=? AND c_id=?", sJoin, cJoin
                                    );

                                    if (!exist.isEmpty()) {
                                        System.out.println("Already a member.");
                                        break;
                                    }

                                    con.updateRecord(
                                            "INSERT INTO membership_tbl (u_id, c_id, position, join_date) VALUES (?, ?, 'Member', CURRENT_DATE)",
                                            sJoin, cJoin
                                    );
                                    System.out.println("Joined club successfully!");
                                    break;

                                case 6:
                                    return;

                                default:
                                    System.out.println("Invalid choice.");
                            }

                            System.out.print("Continue as Student? (Y/N): ");
                            cont = sc.next().charAt(0);

                        } while (cont == 'Y' || cont == 'y');
                    }
                    break;

                case 2:
                    System.out.print("\nEnter user name: ");
                    String name = sc.next();
                    System.out.print("Enter user email: ");
                    String newEmail = sc.next();

                    String emailCheckQry = "SELECT * FROM users_tbl WHERE email=?";
                    List<Map<String, Object>> existing = con.fetchRecords(emailCheckQry, newEmail);

                    System.out.print("Enter user Type (1 - SAO / 2 - AVP / 3 - CLUB OFFICER / 4 - STUDENT): ");
                    int t = sc.nextInt();
                    String tp;

                    if (t == 1) {
                        tp = "SAO";
                    } else if (t == 2) {
                        tp = "AVP";
                    } else if (t == 3) {
                        tp = "CLUB OFFICER";
                    } else {
                        tp = "STUDENT";
                    }

                    System.out.print("Enter Password: ");
                    String p = sc.next();

                    String dept;
                    int yr;

                    if (tp.equals("CLUB OFFICER") || tp.equals("STUDENT")) {
                        System.out.print("Enter Department: ");
                        dept = sc.next();
                        System.out.print("Enter Year Level: ");
                        yr = sc.nextInt();
                    } else {
                        dept = "N/A";
                        yr = 0;
                    }

                    if (!existing.isEmpty()) {
                        Map<String, Object> existUser = existing.get(0);
                        if (existUser.get("role").equals("STUDENT") && tp.equals("CLUB OFFICER")) {
                            con.updateRecord("UPDATE users_tbl SET role=? WHERE email=?", tp, newEmail);
                            System.out.println("Existing student upgraded to CLUB OFFICER!");
                        } else {
                            System.out.println("Email already exists. Use another email.");
                        }
                    } else {
                        con.addRecord(
                                "INSERT INTO users_tbl(name, email, pass, role, department, year_lvl) VALUES (?, ?, ?, ?, ?, ?)",
                                name, newEmail, p, tp, dept, yr
                        );
                    }
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