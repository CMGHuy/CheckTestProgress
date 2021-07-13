import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProgress {

    Map<String, TestProgress> instanceProgress = new HashMap<>();

    public void collectProgress(String allProgressPath) {
        File progressFolder = new File(allProgressPath);

        for (File progressFile : progressFolder.listFiles()) {
            String fileName = progressFile.getName();
            String instanceIPAddress = getIPAddress(fileName);

            try (BufferedReader br = new BufferedReader(new FileReader(progressFile.getAbsolutePath()))) {
                String line;
                float progress = 0;
                int passed = 0, failed = 0, done = 0, remained = 0, assigned = 0;
                while ((line = br.readLine()) != null) {
                    String keywordValue = line.substring(line.lastIndexOf(":") + 1).trim();
                    if (line.contains("Progress")) {
                        progress = Float.parseFloat(keywordValue.replace("%", ""));
                    } else if (line.contains("Passed")) {
                        passed = Integer.parseInt(keywordValue);
                    } else if (line.contains("Failed")) {
                        failed = Integer.parseInt(keywordValue);
                    } else if (line.contains("Done")) {
                        done = Integer.parseInt(keywordValue);
                    } else if (line.contains("Remained")) {
                        remained = Integer.parseInt(keywordValue);
                    } else if (line.contains("Assigned")) {
                        assigned = Integer.parseInt(keywordValue);
                    }
                }

                instanceProgress.put(instanceIPAddress, new TestProgress(progress, passed, failed, done, remained, assigned));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public void getCurrentProgress(String outputFilePath) {
        float currentProgress = 0;
        int currentPassed = 0;
        int currentFailed = 0;
        int currentDone = 0;
        int currentRemained = 0;
        int currentAssigned = 0;

        for (Map.Entry<String, TestProgress> individualProgress : instanceProgress.entrySet()) {
            currentPassed += individualProgress.getValue().getPassedTest();
            currentFailed += individualProgress.getValue().getFailedTest();
            currentDone += individualProgress.getValue().getDoneTest();
            currentRemained += individualProgress.getValue().getRemainedTest();
            currentAssigned += individualProgress.getValue().getAssignedTest();
        }

        currentProgress = (float) currentDone * 100 / currentAssigned;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
            String summary =    "Progress: " + (Math.round(currentProgress * 100.0) / 100.0) + " %" +
                                "\nPassed: " + currentPassed +
                                "\nFailed: " + currentFailed +
                                "\nDone: " + currentDone +
                                "\nRemained: " + currentRemained +
                                "\nAssigned: " + currentAssigned + "\n";
            bw.write(summary);
            bw.flush();
            instanceProgress.clear();
            System.out.println("Total test running progress acquired!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
