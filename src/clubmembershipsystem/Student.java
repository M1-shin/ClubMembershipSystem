package clubmembershipsystem;

import Config.Config;
import java.util.*;

public class Student {
    Scanner sc = new Scanner(System.in);
    Config con = new Config();
    char cont;
    Map<String, Object> user;

    public Student(Map<String, Object> user) {
        this.user = user;
    }

    public void studentDashboard() {
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

                    con.addRecord(
                        "INSERT INTO clubs_tbl (c_name, c_type, dep_club, status, created_by, created_date) VALUES (?, ?, ?, ?, ?, ?)",
                        cName, cType, dep, "Pending", sId, appDate
                    );
                    System.out.println("Club creation request submitted.");
                    break;

                case 2:
                    List<Map<String, Object>> approvedClubs = con.fetchRecords(
                        "SELECT * FROM clubs_tbl WHERE status='Approved'"
                    );

                    if (approvedClubs.isEmpty()) {
                        System.out.println("No records found.\n");
                        break;
                    }

                    con.viewRecords(
                        "SELECT * FROM clubs_tbl WHERE status='Approved'",
                        new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                        new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                    );

                    System.out.print("\nEnter Club ID to join: ");
                    int cJoin = sc.nextInt();
                    int sJoin = Integer.parseInt(user.get("u_id").toString());

                    List<Map<String, Object>> existingMember = con.fetchRecords(
                        "SELECT * FROM membership_tbl WHERE u_id=? AND c_id=?", sJoin, cJoin
                    );

                    if (!existingMember.isEmpty()) {
                        System.out.println("Already a member.");
                        break;
                    }

                    List<Map<String, Object>> memberPos = con.fetchRecords(
                        "SELECT pos_id FROM position_tbl WHERE pos_name='Member'"
                    );

                    if (!memberPos.isEmpty()) {
                        int memberPosId = Integer.parseInt(memberPos.get(0).get("pos_id").toString());
                        con.addRecord(
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
                    List<Map<String, Object>> createdClubs = con.fetchRecords(
                        "SELECT * FROM clubs_tbl WHERE created_by=" + stuId
                    );

                    if (createdClubs.isEmpty()) {
                        System.out.println("No records found.\n");
                        break;
                    }

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
                        System.out.println("No records found.\n");
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
                        System.out.println("No records found.\n");
                        break;
                    }

                    con.viewRecords(
                        removeQry,
                        new String[]{"Club ID", "Club Name", "Type", "Department", "Status"},
                        new String[]{"c_id", "c_name", "c_type", "dep_club", "status"}
                    );

                    System.out.print("\nEnter Club ID to remove: ");
                    int remClubId = sc.nextInt();

                    con.deleteRecord(
                        "DELETE FROM clubs_tbl WHERE c_id=? AND status='Pending'", remClubId
                    );
                    System.out.println("Club application removed successfully!");
                    break;

                case 6:
                    return;

                default:
                    System.out.println("Invalid choice.");
            }

            System.out.print("Continue? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');
    }
}
