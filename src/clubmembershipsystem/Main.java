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

                        String loginQry = "SELECT * FROM users_tbl WHERE email = ? AND pass = ?";
                        List<Map<String, Object>> users = con.fetchRecords(loginQry, email, pass);

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
                        if (posName.equals("president") || posName.equals("vice president") || posName.equals("secretary") || posName.equals("treasurer")) {
                            con.updateRecord("UPDATE users_tbl SET role='CLUB OFFICER' WHERE u_id=?", loggedId);
                            user.put("role", "CLUB OFFICER");
                        }
                    }
                    String role = user.get("role").toString();
                    System.out.println("\nLOGIN SUCCESS!");

if (role.equals("SAO")) {
                         do {
                            System.out.println("\nWELCOME TO SAO DASHBOARD");
                            System.out.println("1. Approve Accounts");
                            System.out.println("2. Approve Application");
                            System.out.println("3. View Clubs");
                            System.out.println("4. Manage Club Members");
                            System.out.println("5. Exit");
                            System.out.print("\nEnter your choice: ");
                            int saoChoice = sc.nextInt();

                            switch (saoChoice) {
                                case 1:
                                    String AccpendingQry = "SELECT * FROM users_tbl WHERE stat = 'Pending'";
                                    List<Map<String, Object>> pendingg = con.fetchRecords(AccpendingQry);

                                    if (pendingg.isEmpty()) {
                                        System.out.println("No pending accounts.");
                                    } else {
                                        con.viewRecords(
                                            AccpendingQry,
                                            new String[]{"User ID", "Name", "Role", "Department", "Year Level", "Status"},
                                            new String[]{"u_id", "name", "role", "department", "year_lvl", "stat"}
                                        );

                                        System.out.print("Enter User ID to approve: ");
                                        int approveId = sc.nextInt();
                                        con.updateRecord("UPDATE users_tbl SET stat='Approved' WHERE u_id=?", approveId);
                                        System.out.println("Account approved!");
                                    }
                                    break;

                                case 2:
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
                                        System.out.println("Club approved!");
                                    }
                                    break;

                                case 3:
                                    con.viewRecords(
                                        "SELECT * FROM clubs_tbl",
                                        new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                        new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                    );
                                    break;

                                case 4:
                                    String approvedClubsQry = "SELECT * FROM clubs_tbl WHERE status='Approved'";
                                    List<Map<String, Object>> approvedClubs = con.fetchRecords(approvedClubsQry);

                                    if (approvedClubs.isEmpty()) {
                                        System.out.println("No approved clubs available.");
                                        break;
                                    }

                                    con.viewRecords(
                                        approvedClubsQry,
                                        new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                        new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                    );

                                    System.out.print("Enter Club ID to manage: ");
                                    int manageClubId = sc.nextInt();

                                    String memQry = "SELECT m.mem_id, u.name, p.pos_name " +
                                                    "FROM membership_tbl m " +
                                                    "JOIN users_tbl u ON m.u_id=u.u_id " +
                                                    "JOIN position_tbl p ON m.pos_id=p.pos_id " +
                                                    "WHERE m.c_id=?";
                                    List<Map<String, Object>> clubMembers = con.fetchRecords(memQry, manageClubId);

                                    System.out.println("\n--- Club Members ---");
                                    if (clubMembers.isEmpty()) {
                                        System.out.println("No members yet.");
                                    } else {
                                        for (Map<String, Object> mem : clubMembers) {
                                            System.out.println("ID: " + mem.get("mem_id") + " | Name: " + mem.get("name") +
                                                    " | Position: " + mem.get("pos_name"));
                                        }
                                    }

                                    System.out.println("\n1. Assign President");
                                    System.out.println("2. Add Member");
                                    System.out.println("3. Remove Member");
                                    System.out.print("\nChoose option: ");
                                    int manageOpt = sc.nextInt();

                                    switch (manageOpt) {
                                        case 1:
                                            System.out.print("Enter Membership ID to assign as President: ");
                                            int presMemId = sc.nextInt();
                                            List<Map<String, Object>> presPos = con.fetchRecords("SELECT pos_id FROM position_tbl WHERE pos_name='President'");
                                            if (!presPos.isEmpty()) {
                                                int presPosId = Integer.parseInt(presPos.get(0).get("pos_id").toString());
                                                con.updateRecord("UPDATE membership_tbl SET pos_id=? WHERE mem_id=?", presPosId, presMemId);

                                                List<Map<String, Object>> memUser = con.fetchRecords("SELECT u_id FROM membership_tbl WHERE mem_id=?", presMemId);
                                                if (!memUser.isEmpty()) {
                                                    int uId = Integer.parseInt(memUser.get(0).get("u_id").toString());
                                                    con.updateRecord("UPDATE users_tbl SET role='CLUB OFFICER' WHERE u_id=?", uId);
                                                }
                                            } else {
                                                System.out.println("President position not found in position_tbl.");
                                            }
                                            break;

                                        case 2:
                                            System.out.print("Enter User ID to add as member: ");
                                            int newUserId = sc.nextInt();

                                            List<Map<String, Object>> posList = con.fetchRecords("SELECT pos_id, pos_name FROM position_tbl");
                                            System.out.println("\nAvailable Positions:");
                                            for (Map<String, Object> pos : posList) {
                                                System.out.println(pos.get("pos_id") + ". " + pos.get("pos_name"));
                                            }

                                            System.out.print("Enter Position ID: ");
                                            int posId = sc.nextInt();

                                            con.addRecord("INSERT INTO membership_tbl (u_id, c_id, pos_id, join_date) VALUES (?, ?, ?, CURRENT_DATE)", newUserId, manageClubId, posId);
                                            
                                            List<Map<String, Object>> posCheck = con.fetchRecords("SELECT pos_name FROM position_tbl WHERE pos_id=?", posId);
                                            if (!posCheck.isEmpty()) {
                                                String posName = posCheck.get(0).get("pos_name").toString().toLowerCase();
                                                if (posName.equals("president") || posName.equals("vice president") || posName.equals("secretary") || posName.equals("treasurer")) {
                                                    con.updateRecord("UPDATE users_tbl SET role='CLUB OFFICER' WHERE u_id=?", newUserId);
                                                } else {
                                                    con.updateRecord("UPDATE users_tbl SET role='STUDENT' WHERE u_id=?", newUserId);
                                                }
                                            }
                                            break;

                                        case 3:
                                            System.out.print("Enter Membership ID to remove: ");
                                            int removeMem = sc.nextInt();
                                            con.updateRecord("DELETE FROM membership_tbl WHERE mem_id=?", removeMem);
                                            System.out.println("Member removed successfully!");
                                            break;

                                        default:
                                            System.out.println("Invalid option.");
                                    }
                                    break;

                                case 5:
                                    break;

                                default:
                                    System.out.println("Invalid choice.");
                            }

                            System.out.print("Continue as SAO? (Y/N): ");
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
                                    System.out.println("3. View Members");
                                    System.out.println("4. Remove Member");
                                    System.out.print("\nChoose option: ");
                                    int manageChoice = sc.nextInt();
                                    sc.nextLine();

                                    switch (manageChoice) {
                                        case 1:
                                            System.out.print("\nEnter User ID of new member: ");
                                            int userId = sc.nextInt();
                                            sc.nextLine();

                                            List<Map<String, Object>> posListAdd = con.fetchRecords("SELECT pos_id, pos_name FROM position_tbl");
                                            System.out.println("\nAvailable Positions:");
                                            for (Map<String, Object> pos : posListAdd) {
                                                System.out.println(pos.get("pos_id") + ". " + pos.get("pos_name"));
                                            }

                                            System.out.print("Enter Position ID: ");
                                            int positionId = sc.nextInt();

                                            List<Map<String, Object>> checkDup =
                                                    con.fetchRecords("SELECT * FROM membership_tbl WHERE u_id=? AND c_id=?", userId, clubId);

                                            if (!checkDup.isEmpty()) {
                                                System.out.println("User already a member.");
                                                break;
                                            }

                                            con.updateRecord(
                                                    "INSERT INTO membership_tbl (u_id, c_id, pos_id, join_date) VALUES (?, ?, ?, CURRENT_DATE)",
                                                    userId, clubId, positionId
                                            );
                                            System.out.println("Member added successfully!");
                                            break;

                                        case 2:
                                            String presidentCheckQry =
                                                "SELECT p.pos_name FROM membership_tbl m " +
                                                "JOIN users_tbl u ON m.u_id = u.u_id " +
                                                "JOIN position_tbl p ON m.pos_id = p.pos_id " +
                                                "WHERE u.email=? AND m.c_id=?";
                                            List<Map<String, Object>> presCheck = con.fetchRecords(presidentCheckQry, officerEmail, clubId);

                                            if (presCheck.isEmpty() || 
                                                !presCheck.get(0).get("pos_name").toString().equalsIgnoreCase("President")) {
                                                System.out.println("Only the President can edit member positions.");
                                                break;
                                            }

                                            System.out.print("\nEnter Membership ID to edit: ");
                                            int memEdit = sc.nextInt();
                                            sc.nextLine();

                                            List<Map<String, Object>> posList = con.fetchRecords("SELECT pos_id, pos_name FROM position_tbl");
                                            System.out.println("\nAvailable Positions:");
                                            for (Map<String, Object> pos : posList) {
                                                System.out.println(pos.get("pos_id") + ". " + pos.get("pos_name"));
                                            }

                                            System.out.print("Enter New Position ID: ");
                                            int newPosId = sc.nextInt();

                                            con.updateRecord("UPDATE membership_tbl SET pos_id=? WHERE mem_id=?", newPosId, memEdit);
                                            System.out.println("Position updated successfully!");
                                            break;

                                        case 3:
                                            String viewQry =
                                                "SELECT m.mem_id, u.name, u.email, p.pos_name, m.join_date " +
                                                "FROM membership_tbl m " +
                                                "JOIN users_tbl u ON m.u_id = u.u_id " +
                                                "JOIN position_tbl p ON m.pos_id = p.pos_id " +
                                                "WHERE m.c_id = " + clubId;

                                            String[] headers = {"Membership ID", "Member Name", "Email", "Position", "Join Date"};
                                            String[] columns = {"mem_id", "name", "email", "pos_name", "join_date"};

                                            con.viewRecords(viewQry, headers, columns);
                                            break;
                                            
                                        case 4:
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
                                        "SELECT * FROM clubs_tbl WHERE status='Approved'",
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

                                    List<Map<String, Object>> memberPos = con.fetchRecords("SELECT pos_id FROM position_tbl WHERE pos_name='Member'");
                                    if (!memberPos.isEmpty()) {
                                        int memberPosId = Integer.parseInt(memberPos.get(0).get("pos_id").toString());
                                        con.updateRecord(
                                            "INSERT INTO membership_tbl (u_id, c_id, pos_id, join_date) VALUES (?, ?, ?, CURRENT_DATE)",
                                            sJoin, cJoin, memberPosId
                                        );
                                        System.out.println("Joined club successfully!");
                                    } else {
                                        System.out.println("Default position 'Member' not found in position_tbl.");
                                    }
                                    break;
                                    
                                case 3:
                                    int stuId = Integer.parseInt(user.get("u_id").toString());
                                    con.viewRecords(
                                        "SELECT * FROM clubs_tbl WHERE created_by=" + stuId,
                                        new String[]{"Club ID", "Club Name", "Type", "Department", "Status", "Created Date"},
                                        new String[]{"c_id", "c_name", "c_type", "dep_club", "status", "created_date"}
                                    );
                                    break;
                                        
                                case 4:
                                    int stuUpdateId = Integer.parseInt(user.get("u_id").toString());

                                    String updateQry = "SELECT * FROM clubs_tbl WHERE created_by=" + stuUpdateId + " AND status='Pending'";

                                    List<Map<String, Object>> pendingClub = con.fetchRecords(updateQry);

                                    if (pendingClub.isEmpty()) {
                                        System.out.println("No pending records.\n");
                                        break;
                                    }
                                    
                                    con.viewRecords(
                                        updateQry,
                                        new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                        new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                    );

                                    System.out.print("\nEnter Club ID to update: ");
                                    int upClubId = sc.nextInt();
                                    sc.nextLine();
                                    System.out.print("Enter New Club Name: ");
                                    String newName = sc.nextLine();
                                    System.out.print("Enter New Club Type: ");
                                    String newType = sc.nextLine();
                                    System.out.print("Enter New Department: ");
                                    String newDep = sc.nextLine();

                                    con.updateRecord(
                                        "UPDATE clubs_tbl SET c_name=?, c_type=?, dep_club=? WHERE c_id=? AND status='Pending'",
                                        newName, newType, newDep, upClubId
                                    );

                                    System.out.println("Club application updated successfully!");
                                    break;
                                        
                                case 5:
                                    int stuRemoveId = Integer.parseInt(user.get("u_id").toString());

                                    String removeQry = "SELECT * FROM clubs_tbl WHERE created_by=" + stuRemoveId + " AND status='Pending'";

                                    List<Map<String, Object>> pendingClubs = con.fetchRecords(removeQry);

                                    if (pendingClubs.isEmpty()) {
                                        System.out.println("No pending records.\n");
                                        break;
                                    }
                                    
                                    con.viewRecords(
                                        removeQry,
                                        new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                                        new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                                    );

                                    System.out.print("\nEnter Club ID to remove: ");
                                    int remClubId = sc.nextInt();

                                    con.deleteRecord("DELETE FROM clubs_tbl WHERE c_id=? AND status='Pending'", remClubId);

                                    System.out.println("Club application removed successfully!");
                                    break;


                                case 6:
                                    return;

                                default:
                                    System.out.println("Invalid choice.");
                            }

                            System.out.print("Continue? (Y/N): \n");
                            cont = sc.next().charAt(0);

                        } while (cont == 'Y' || cont == 'y');
                    }
                    break;

                case 2:
                    String utp = null, newEmail;

                    System.out.print("\nEnter user name: ");
                    String name = sc.next();
                    
                    while (true){
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
                                name, newEmail, p, utp, dept, yr, "Pending"
                        );
                        System.out.println("Account approval is pending...");
                        break;
                   

                case 3:
                    System.out.println("Thank you! Exiting system.");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }

            System.out.print("\nDo you want to continue? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');

        System.out.println("Program Ended.");
    }
}