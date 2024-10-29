package org.CLI;

import java.util.*;
import java.io.*;

public class CommandLineInterpreter {
    private File currentDirectory;
    private Scanner scanner;
    private StringTokenizer stringTokenizer;

    CommandLineInterpreter() {
        this.currentDirectory = new File(System.getProperty("user.dir"));
        this.scanner = new Scanner(System.in);
        System.out.print("\033[H\033[2J");
        System.out.flush();
        this.execute();
    }

    public void execute() {
        while(true) {
            System.out.print(currentDirectory.getAbsolutePath() + ": ");
            String input = scanner.nextLine();
            this.stringTokenizer = new StringTokenizer(input, " ");
            String command = stringTokenizer.nextToken();
            switch (command) {
                case "exit": {
                    return;
                }
                case "help": {
                    this.help();
                    break;
                }
                case "pwd": {
                    this.pwd();
                    break;
                }
                case "cd": {

                    this.cd();
                    break;
                }
                case "ls": {
                    this.ls();
                    break;
                }
                case "mkdir": {
                    this.mkdir();
                    break;
                }
                case "rmdir": {
                    this.rmdir();
                    break;
                }
                case "touch": {
                    this.touch();
                    break;
                }
                case "rm": {
                    this.rm();
                    break;
                }
                case "cat": {
                    this.cat();
                    break;
                }
                case "mv": {
                    this.mv();
                    break;
                }
                default: {
                    break;
                }
            }

        }
    }

    public void exit() {

    }

    public String help() {
        return "";
    }

    public String pwd() {
        return System.getProperty("user.dir");
    }

    public boolean cd() {
        if (stringTokenizer.countTokens() > 2) {
            System.out.println("Wrong Command");
            return false;
        }
        String targetDirectory = stringTokenizer.nextToken();

        File newDir = new File(targetDirectory);
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

    public String[] ls() {
        if (this.stringTokenizer.countTokens() > 2) {
            System.out.println("Wrong Command");
            return null;
        }

        boolean showAll = false;
        boolean recursive = false;

        while (this.stringTokenizer.hasMoreTokens()) {
            String option = this.stringTokenizer.nextToken();
            if (option.equals("-a")) {
                showAll = true;
            } else if (option.equals("-r")) {
                recursive = true;
            } else {
                System.out.println("Invalid option: " + option);
                return null;
            }
        }

        List<String> fileList = listFiles(this.currentDirectory, showAll, recursive, "");
        return fileList.toArray(new String[0]);
    }

    private List<String> listFiles(File directory, boolean showAll, boolean recursive, String indent) {
        List<String> fileList = new ArrayList<>();

        if (!directory.isDirectory()) {
            System.out.println(directory.getName() + " is not a directory");
            return fileList;
        }

        String[] files = directory.list();
        if (files == null) {
            return fileList;
        }

        for (String fileName : files) {
            File file = new File(directory, fileName);

            if (file.isHidden() && !showAll) {
                continue;
            }

            fileList.add(indent + file.getName());

            if (recursive && file.isDirectory()) {
                fileList.addAll(listFiles(file, showAll, true, indent + "  "));
            }
        }
        return fileList;
    }


    public boolean mkdir() {
        if (!this.stringTokenizer.hasMoreTokens()) {
            System.out.println("Usage: mkdir <directory_name> [additional_directory_names...]");
            return false;
        }

        while (this.stringTokenizer.hasMoreTokens()) {
            String dirName = this.stringTokenizer.nextToken();
            File newDir = new File(this.currentDirectory, dirName);

            if (newDir.exists()) {
                System.out.println("Directory '" + dirName + "' already exists.");
                continue;
            }

            if (newDir.mkdir()) {
                System.out.println("Directory '" + dirName + "' created successfully.");
            } else {
                System.out.println("Error: Could not create directory '" + dirName + "'. Please check the name and try again.");
            }
        }
        return true;
    }

    public boolean rmdir() {
        String fileName = this.stringTokenizer.nextToken();
        File newFile = new File(fileName);
        return newFile.delete();
    }

    public boolean touch() {
        if (!this.stringTokenizer.hasMoreTokens()) {
            System.out.println("Usage: touch <file_name> [additional_file_names...]");
            return false;
        }

        while (this.stringTokenizer.hasMoreTokens()) {
            String fileName = this.stringTokenizer.nextToken();
            File newFile = new File(this.currentDirectory, fileName);

            try {
                if (newFile.exists()) {
                    System.out.println("File '" + fileName + "' already exists.");
                    continue;
                }

                if (newFile.createNewFile()) {
                    System.out.println("File '" + fileName + "' created successfully.");
                } else {
                    System.out.println("Error: Could not create file '" + fileName + "'.");
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public void mv() {
        if (this.stringTokenizer.countTokens() != 3) {
            System.out.println("Usage: mv <source> <destination>");
            return;
        }

        String sourcePath = this.stringTokenizer.nextToken();
        String destinationPath = this.stringTokenizer.nextToken();

        File sourceFile = new File(this.currentDirectory, sourcePath);
        File destinationFile = new File(this.currentDirectory, destinationPath);

        if (!sourceFile.exists()) {
            System.out.println("Error: Source file does not exist");
            return;
        }

        if (sourceFile.renameTo(destinationFile)) {
            System.out.println("File moved successfully");
        } else {
            System.out.println("Error: Unable to move the file");
        }
    }

    public void rm() {
        String fileName = this.stringTokenizer.nextToken();
        File newFile = new File(fileName);
        if (newFile.isDirectory()) {
            System.out.println("Can't remove a directory using this command, try rmdir");
        }
        if (!newFile.delete()) {
            System.out.println("Please enter a valid file name");
        }
    }

    public void cat() {
        if (this.stringTokenizer.countTokens() > 2) {
            System.out.println("Usage: cat <file_name>");
            return;
        }
        File file = new File(this.stringTokenizer.nextToken());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());;
        }
    }

}
