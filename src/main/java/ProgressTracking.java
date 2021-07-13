import org.apache.commons.cli.*;

public class ProgressTracking {

    /*
        After collecting subreport_text files from worker instances, this application is used to generate the
        summarized information. Firstly, the simplified information is extracted from each subreport_text file.
        Then, the summation of those information are exported.

        The old files in subreport_text and currentProgress folder are cleaned up for every run. It ensures that
        the files are always on new state, not be appended.
     */

    public static void main(String[] args) {

        Options options = new Options();

        Option retrieve = new Option("r", "retrieve", true, "retrieve test progress from other instances" +
                "\nthree arguments in order respectively:" +
                "\ndivided master suite folder path" +
                "\nsub report text folder path" +
                "\nprogress output location folder path");
        retrieve.setArgs(3);
        options.addOption(retrieve);

        Option update = new Option("u", "update", true, "update overall test progress " +
                "\ntwo arguments in order respectively:" +
                "\nfolder containing all instances progress path" +
                "\noutput current progress file path");
        options.addOption(update);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("r")) {
//                -r is already the args[0]
//                Example:
//                String folderMasterSuitePath = "C:\\Users\\h.cao\\Desktop\\masterSuite";
//                String folderSubReportTextPath = "C:\\Users\\h.cao\\Desktop\\subreport_text";
//                String folderCurrentProgressPath = "C:\\Users\\h.cao\\Desktop\\currentProgress";
                String folderMasterSuitePath = args[1];
                String folderSubReportTextPath = args[2];
                String folderCurrentProgressPath = args[3];

                RetrieveProgress testProgress = new RetrieveProgress();
                testProgress.getAllCurrentProgress(folderMasterSuitePath, folderSubReportTextPath, folderCurrentProgressPath);
            }
            if (cmd.hasOption("u")) {
//                -u is already the args[0]
//                Example:
//                String progressFolderPath = "C:\\Users\\h.cao\\Desktop\\currentProgress";
//                String progressOutputPath = "C:\\Users\\h.cao\\Desktop\\currentProgress\\currentProgress.txt";
                String progressFolderPath = args[1];
                String progressOutputPath = args[2];

                UpdateProgress updateProgress = new UpdateProgress();

                updateProgress.collectProgress(progressFolderPath);
                updateProgress.getCurrentProgress(progressOutputPath);
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("usage:", options);

            System.exit(1);
        }
    }
}
