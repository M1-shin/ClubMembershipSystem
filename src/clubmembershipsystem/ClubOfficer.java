package clubmembershipsystem;

import Config.Config;
import java.util.*;

public class ClubOfficer {

    Scanner sc = new Scanner(System.in);
    Config con = new Config();
    char cont;
    Map<String, Object> user;

    public ClubOfficer(Map<String, Object> user) {
        this.user = user;
    }

    public void officerDashboard() {
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
                                System.out.println("User is already a member.");
                                break;
                            }

                            con.addRecord(
                                "INSERT INTO membership_tbl (u_id, c_id, pos_id, join_date) VALUES (?, ?, ?, CURRENT_DATE)",
                                userId, clubId, positionId
                            );
                            System.out.println("✅ Member added successfully!");
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
                            System.out.println("✅ Position updated successfully!");
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
                            con.deleteRecord("DELETE FROM membership_tbl WHERE mem_id=?", memRemove);
                            System.out.println("✅ Member removed successfully!");
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
    }
}
