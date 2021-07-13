import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetrieveProgress {

    int totalAssignedTest = 0, totalTestDone = 0;
    int totalTestPass = 0, totalTestFail = 0;

    ArrayList<String> testPass = new ArrayList<>();
    ArrayList<String> testFail = new ArrayList<>();

    public void getAllCurrentProgress(String folderMasterSuitePath, String folderSubReportTextPath, String folderCurrentProgressPath) {
        File masterSuiteFolder = new File(folderMasterSuitePath);
        File subReportTextFolder = new File(folderSubReportTextPath);
        File currentProgressFolder = new File(folderCurrentProgressPath);

        ArrayList<File> masterSuiteList = new ArrayList<>(Arrays.asList(masterSuiteFolder.listFiles()));
        ArrayList<File> subReportTextList = new ArrayList<>(Arrays.asList(subReportTextFolder.listFiles()));
        // Mapping master suite file with sub report text file
        Map<File, File> instancesTestInfo = new HashMap<>();

        // Remove all old progress files
        emptyFolder(currentProgressFolder);

        // Get the correct file master suite and file sub report text
        for (File masterSuiteFile : masterSuiteList) {
            String masterSuiteFileName = masterSuiteFile.getName();
            String masterSuiteFileIP = getIPAddress(masterSuiteFileName);

            for (File subReportTextFile : subReportTextList) {
                String subReportTextFileName = subReportTextFile.getName();
                String subReportTextFileIP = getIPAddress(subReportTextFileName);
                if (masterSuiteFileIP.equalsIgnoreCase(subReportTextFileIP)) {
                    getTotalAssignedTests(masterSuiteFile.getAbsolutePath());
                    getCurrentProgress(subReportTextFile.getAbsolutePath());
                    exportProgressInfo(folderCurrentProgressPath + "//TestRunProgress-" + subReportTextFileIP + ".txt");
                }
            }
        }

        emptyFolder(subReportTextFolder);
    }

    public String getIPAddress(String fileName) {
        // Regex pattern for ip address
        Pattern pattern = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    // Get total test assigned to this worker instance
    public void getTotalAssignedTests(String fileMasterSuitePath) {
        Set<String> totalTest = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileMasterSuitePath), StandardCharsets.UTF_16LE))){
            String line;
            // Remove the first line
            line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] lineParameters = line.split("\t");
                // Test name column
                totalTest.add(lineParameters[13]);
            }

            totalAssignedTest = totalTest.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*  Get current num of test failed, passed, other parameters
        Example of progress file. Delimiter is \t
        Sl_no	 Testcase_id	Testcasename	Result	Scrum_team	Product
        1	1-2ARVXC	[1][RT]CFM_CR0544_02_Login with Updated user _ Part 2_Test 1	PASS
        2	1-1OFZ5A	[1][RT]Sales_UC1240_01_Manage user task list	PASS
     */
    public void getCurrentProgress(String fileSubReportText) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileSubReportText))){
            String line;

            // Remove the first 2 blank lines and title line
            line = br.readLine();
            line = br.readLine();
            line = br.readLine();

            // Start processing
            while ((line = br.readLine()) != null) {
                String[] lineParameters = line.split("\t");
                if (lineParameters[3].equalsIgnoreCase("PASS")) {
                    testPass.add(lineParameters[2]);
                    totalTestPass++;
                } else {
                    testFail.add(lineParameters[2]);
                    totalTestFail++;
                }
                totalTestDone++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Export the test progress into a .txt file.
        The old file will be overwritten instead of appended by the next execution.
        Example:
        Master Suite Name	Product	Scrum Team	Test Set Id	Test Set Skip	Test Set Sequence	Test Script Sequence	Test Script Id	Test Script Skip	Test Script Skip On Prior Abort	Test Plan ID	Test Case ID	Test Set Description	Test Script Description	Test Step Description	Action	Target Object	Inputs	End Action	Screenshot Required	Test Step Sequence	Test Step Id	QA Test Plan Id	QA Test Plan Name	Test Set Name	DataSet Name	Test Set Iterate	Test Script Iterate
        C2CTestSuiteINT			1-7JXW9N	N	1	1	1-1OAUXL	N	N			Sales Sanity Checks Universal	[1]Sales_CR_669-Enhance the Cheetah Admin Role with iHelp	Recorded	Launch		Siebel Sales Enterprise;U101313;Y		N	1	1-1OAUXM			Sales Sanity Checks Universal		N	N
        C2CTestSuiteINT			1-7JXW9N	N	1	1	1-1OAUXL	N	N			Sales Sanity Checks Universal	[1]Sales_CR_669-Enhance the Cheetah Admin Role with iHelp	Recorded	ClickLink	NULL|Toggle Task Assistant	NULL;IPH2;IPH3		N	2	1-1OAUXN			Sales Sanity Checks Universal		N	N
     */
    public void exportProgressInfo(String outputLocation) {
        float percentage = (float) totalTestDone * 100 / totalAssignedTest;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputLocation))) {
            String summary =    "Progress: " + (Math.round(percentage * 100.0) / 100.0) + "%" +
                                "\nPassed: " + totalTestPass +
                                "\nFailed: " + totalTestFail +
                                "\nDone: " + totalTestDone +
                                "\nRemained: " + (totalAssignedTest - totalTestDone) +
                                "\nAssigned: " + totalAssignedTest;
            System.out.println("Instance test running progress acquired!!");
            refreshVariable();
            bw.write(summary);
            bw.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void emptyFolder(File folder) {
        for (File file : folder.listFiles()) {
            file.delete();
        }
    }

    // Reinitialize all these variables
    public void refreshVariable(){
        totalAssignedTest = 0;
        totalTestFail = 0;
        totalTestPass = 0;
        totalTestDone = 0;
    }
}
