package clubmembershipsystem;
import Config.Config;
import java.util.*;

public class SAO {
    Scanner sc = new Scanner(System.in);
    Config con = new Config();
    char cont;

    public void SAODashboard() {
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

                    String memQry =
                        "SELECT m.mem_id, u.name, p.pos_name " +
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
                            System.out.println(
                                "ID: " + mem.get("mem_id") +
                                " | Name: " + mem.get("name") +
                                " | Position: " + mem.get("pos_name")
                            );
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
                            List<Map<String, Object>> presPos =
                                con.fetchRecords("SELECT pos_id FROM position_tbl WHERE pos_name='President'");
                            if (!presPos.isEmpty()) {
                                int presPosId = Integer.parseInt(presPos.get(0).get("pos_id").toString());
                                con.updateRecord("UPDATE membership_tbl SET pos_id=? WHERE mem_id=?", presPosId, presMemId);

                                List<Map<String, Object>> memUser =
                                    con.fetchRecords("SELECT u_id FROM membership_tbl WHERE mem_id=?", presMemId);
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

                            List<Map<String, Object>> posList =
                                con.fetchRecords("SELECT pos_id, pos_name FROM position_tbl");
                            System.out.println("\nAvailable Positions:");
                            for (Map<String, Object> pos : posList) {
                                System.out.println(pos.get("pos_id") + ". " + pos.get("pos_name"));
                            }

                            System.out.print("Enter Position ID: ");
                            int posId = sc.nextInt();

                            con.addRecord(
                                "INSERT INTO membership_tbl (u_id, c_id, pos_id, join_date) VALUES (?, ?, ?, CURRENT_DATE)",
                                newUserId, manageClubId, posId
                            );

                            List<Map<String, Object>> posCheck =
                                con.fetchRecords("SELECT pos_name FROM position_tbl WHERE pos_id=?", posId);
                            if (!posCheck.isEmpty()) {
                                String posName = posCheck.get(0).get("pos_name").toString().toLowerCase();
                                if (posName.equals("president") || posName.equals("vice president")
                                        || posName.equals("secretary") || posName.equals("treasurer")) {
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
    }
}
