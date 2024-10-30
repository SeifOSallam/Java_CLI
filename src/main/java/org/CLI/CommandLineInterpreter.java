package org.CLI;

import java.util.*;
import java.io.*;

public class CommandLineInterpreter {
    private File currentDirectory;
    private final Scanner scanner;
    private StringTokenizer stringTokenizer;

    CommandLineInterpreter() {
        this.currentDirectory = new File(System.getProperty("user.dir"));
        this.scanner = new Scanner(System.in);
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void execute() {
        while (true) {
            System.out.print(currentDirectory.getAbsolutePath() + ": ");
            String input = scanner.nextLine();
            if (!processInput(input)) {
                break; // Exit command
            }
        }
    }

    public boolean processInput(String input) {
        this.stringTokenizer = new StringTokenizer(input, " ");
        String command = stringTokenizer.nextToken();
        List<String> commandArguments = new ArrayList<>();
        while(stringTokenizer.hasMoreElements()) {
            commandArguments.add(stringTokenizer.nextToken());
        }
        switch (command) {
            case "exit": {
                return false;
            }
            case "help": {
                String[] res = this.help();
                for (String help : res) {
                    System.out.println(help);
                }
                break;
            }
            case "pwd": {
                System.out.println(this.pwd());
                break;
            }
            case "cd": {
                this.cd(commandArguments);
                break;
            }
            case "ls": {
                String[] res = this.executeLs(commandArguments);
                if (res != null) {
                    for (String s : res) {
                        System.out.println(s);
                    }
                }
                break;
            }
            case "mkdir": {
                this.mkdir(commandArguments);
                break;
            }
            case "rmdir": {
                this.rmdir(commandArguments);
                break;
            }
            case "touch": {
                this.touch(commandArguments);
                break;
            }
            case "rm": {
                this.rm(commandArguments);
                break;
            }
            case "cat": {
                String[] res = this.executeCat(commandArguments);
                if (res != null) {
                    for (String s : res) {
                        System.out.println(s);
                    }
                }
                break;
            }
            case "mv": {
                this.mv(commandArguments);
                break;
            }
            default: {
                break;
            }
        }
        return true;
    }

    public String[] help() {
        List<String> helpMessages = new ArrayList<>();

        helpMessages.add("Available Commands:");
        helpMessages.add("1. pwd         : Prints the current working directory.");
        helpMessages.add("2. cd <dir>   : Changes the current directory to <dir>.");
        helpMessages.add("3. ls         : Lists files in the current directory.");
        helpMessages.add("4. ls -a      : Lists all files, including hidden files.");
        helpMessages.add("5. ls -r      : Lists files in reverse order.");
        helpMessages.add("6. mkdir <dir>: Creates a new directory named <dir>.");
        helpMessages.add("7. rmdir <dir>: Removes an empty directory named <dir>.");
        helpMessages.add("8. touch <file>: Creates a new file named <file>.");
        helpMessages.add("9. mv <src> <dest>: Moves or renames a file or directory.");
        helpMessages.add("10. rm <file> : Removes a file named <file>.");
        helpMessages.add("11. cat <file>: Displays the contents of <file>.");
        helpMessages.add("12. > <file>  : Redirects output to <file> (overwrites).");
        helpMessages.add("13. >> <file> : Redirects output to <file> (appends).");
        helpMessages.add("14. |         : Pipes the output of one command to another.");
        helpMessages.add("15. exit      : Terminates the CLI.");
        helpMessages.add("16. help      : Displays this help message.");

        // Convert List<String> to String[] and return
        return helpMessages.toArray(new String[0]);
    }

    public String pwd() {
        return this.currentDirectory.getAbsolutePath();
    }

    public boolean cd(List<String> commandArguments) {
        if (commandArguments.size() != 1) {
            System.out.println("Wrong Command");
            return false;
        }
        String targetDirectory = commandArguments.getFirst();

        File newDir = new File(this.currentDirectory, targetDirectory);
        if (targetDirectory.equals("..")) {
            newDir = new File(currentDirectory.getParent());
        }
        if (!newDir.exists() || !newDir.isDirectory()) {
            System.out.println(newDir.getName());
            System.out.println("Directory Doesn't Exist");
            return false;
        }
        this.currentDirectory = new File(newDir.getAbsolutePath());
        return true;
    }

    public String[] executeLs(List<String> commandArguments) {
        boolean showAll = false;
        boolean reverse = false;
        String outputFileName = null;
        boolean append = false;

        // Parse options and detect redirection
        for (String option : commandArguments) {
            switch (option) {
                case "-a":
                    showAll = true;
                    break;
                case "-r":
                    reverse = true;
                    break;
                case ">":
                    append = false;
                    outputFileName = getNextArgument(option, commandArguments);
                    break;
                case ">>":
                    append = true;
                    outputFileName = getNextArgument(option, commandArguments);
                    break;
                default:
                    return null;
            }

            // Stop further parsing if redirection is detected
            if (outputFileName != null) break;
        }

        // Call ls function to get the list of files
        String[] result = ls(showAll, reverse);

        // Prepare output
        StringBuilder output = new StringBuilder();
        for (String file : result) {
            output.append(file).append("\n");
        }

        // Redirect output if needed
        if (outputFileName != null) {
            if (append) {
                appendOutput(output.toString(), outputFileName);
            } else {
                redirectOutput(output.toString(), outputFileName);
            }
        }
        return result;
    }

    // Helper function to handle `ls` listing based on flags
    private String[] ls(boolean showAll, boolean reverse) {
        File[] allFiles = this.currentDirectory.listFiles();
        if (allFiles == null) {
            return new String[0];
        }

        List<String> filteredFiles = new ArrayList<>();
        for (File file : allFiles) {
            if (showAll || !file.isHidden()) {
                filteredFiles.add(file.getName());
            }
        }

        Collections.sort(filteredFiles);

        if (reverse) {
            Collections.reverse(filteredFiles);
        }

        return filteredFiles.toArray(new String[0]);
    }

    // Get the next argument after `>` or `>>` for the output file name
    private String getNextArgument(String currentOption, List<String> commandArguments) {
        int index = commandArguments.indexOf(currentOption);
        if (index >= 0 && index + 1 < commandArguments.size()) {
            return commandArguments.get(index + 1);
        } else {
            System.out.println("Error: No output file specified for " + currentOption);
            return null;
        }
    }




    public boolean mkdir(List<String> commandArguments) {
        if (commandArguments.isEmpty()) {
            System.out.println("Usage: mkdir <directory_name> [additional_directory_names...]");
            return false;
        }

        for (String argument: commandArguments) {
            File newDir = new File(this.currentDirectory, argument);

            if (newDir.exists()) {
                System.out.println("Directory '" + argument + "' already exists.");
                continue;
            }

            if (newDir.mkdir()) {
                System.out.println("Directory '" + argument + "' created successfully.");
            } else {
                System.out.println("Error: Could not create directory '" + argument + "'. Please check the name and try again.");
            }
        }
        return true;
    }

    public boolean rmdir(List<String> commandArguments) {
        File newFile = new File(this.currentDirectory, commandArguments.getFirst());
        return newFile.delete();
    }

    public boolean touch(List<String> commandArguments) {
        if (commandArguments.isEmpty()) {
            System.out.println("Usage: touch <file_name> [additional_file_names...]");
            return false;
        }

        for (String argument : commandArguments) {
            File newFile = new File(this.currentDirectory, argument);

            try {
                if (newFile.exists()) {
                    System.out.println("File '" + argument + "' already exists.");
                    continue;
                }

                if (newFile.createNewFile()) {
                    System.out.println("File '" + argument + "' created successfully.");
                } else {
                    System.out.println("Error: Could not create file '" + argument + "'.");
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean mv(List<String> commandArguments) {
        if (commandArguments.size() != 2) {
            System.out.println("Usage: mv <source> <destination>");
            return false;
        }

        File sourceFile = new File(this.currentDirectory, commandArguments.getFirst());
        File destinationFile = new File(this.currentDirectory, commandArguments.getLast());

        if (!sourceFile.exists()) {
            System.out.println("Error: Source file does not exist");
            return false;
        }
        if (destinationFile.isDirectory()) {
            destinationFile = new File(destinationFile, sourceFile.getName());
        }
        if (sourceFile.renameTo(destinationFile)) {
            System.out.println("File moved successfully");
            return true;
        } else {
            System.out.println("Error: Unable to move the file");
            return false;
        }
    }

    public boolean rm(List<String> commandArguments) {
        for (String fileName : commandArguments) {
            File newFile = new File(this.currentDirectory, fileName);
            if (newFile.isDirectory()) {
                System.out.println("Can't remove a directory using this command, try rmdir");
                return false;
            }
            if (!newFile.delete()) {
                System.out.println("Please enter a valid file name");
                return false;
            }
        }

        return true;
    }

    public String[] executeCat(List<String> commandArguments) {
        // Validate command arguments
        if (commandArguments.isEmpty()) {
            System.out.println("Usage: cat <file_name> [> <output_file>] [>> <output_file>]");
            return null;
        }

        String fileName = commandArguments.getFirst();
        File file = new File(this.currentDirectory, fileName);
        List<String> res = new ArrayList<>();
        String outputFileName = null;
        boolean append = false;

        // Check for redirection in the command arguments
        for (int i = 1; i < commandArguments.size(); i++) {
            String option = commandArguments.get(i);
            if (">".equals(option) || ">>".equals(option)) {
                if (i + 1 < commandArguments.size()) {
                    outputFileName = commandArguments.get(i + 1);
                    append = ">>".equals(option);
                    break; // Stop processing after finding redirection
                } else {
                    System.out.println("Error: No output file specified after " + option);
                    return null;
                }
            }
        }

        // Read the file content
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                res.add(line); // Store the line in the result list
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        // Handle output redirection
        if (outputFileName != null) {
            String output = String.join("\n", res);
            if (append) {
                appendOutput(output, outputFileName);
            } else {
                redirectOutput(output, outputFileName);
            }
        }

        // Convert List<String> to String[] and return
        return res.toArray(new String[0]);
    }

    // Function to overwrite output in a file (for `>` redirection)
    public void redirectOutput(String output, String fileName) {
        try (FileWriter writer = new FileWriter(this.currentDirectory.getName() + '/' + fileName, false)) {  // false means overwrite
            writer.write(output);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    // Function to append output to a file (for `>>` redirection)
    public void appendOutput(String output, String fileName) {
        try (FileWriter writer = new FileWriter(this.currentDirectory.getName() + '/' + fileName, true)) {  // true means append
            writer.write(output);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

}
