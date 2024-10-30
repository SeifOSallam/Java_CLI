package org.CLI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CommandLineInterpreterTest {

    private CommandLineInterpreter cli;

    @BeforeEach
    public void setUp() {
        cli = new CommandLineInterpreter(); // Or use a mock as needed
        cli.mkdir(List.of("testDir"));
        cli.cd(List.of("testDir"));
    }

    @Test
    void help() {
        CommandLineInterpreter CLI = new CommandLineInterpreter();
        CLI.processInput("help");
        String[] expectedHelpMessages = {
                "Available Commands:",
                "1. pwd         : Prints the current working directory.",
                "2. cd <dir>   : Changes the current directory to <dir>.",
                "3. ls         : Lists files in the current directory.",
                "4. ls -a      : Lists all files, including hidden files.",
                "5. ls -r      : Lists files in reverse order.",
                "6. mkdir <dir>: Creates a new directory named <dir>.",
                "7. rmdir <dir>: Removes an empty directory named <dir>.",
                "8. touch <file>: Creates a new file named <file>.",
                "9. mv <src> <dest>: Moves or renames a file or directory.",
                "10. rm <file> : Removes a file named <file>.",
                "11. cat <file>: Displays the contents of <file>.",
                "12. > <file>  : Redirects output to <file> (overwrites).",
                "13. >> <file> : Redirects output to <file> (appends).",
                "14. |         : Pipes the output of one command to another.",
                "15. exit      : Terminates the CLI.",
                "16. help      : Displays this help message."
        };

        // Act
        String[] actualHelpMessages = CLI.help();

        // Assert
        assertArrayEquals(expectedHelpMessages, actualHelpMessages, "Help messages should match expected output.");
    }

    @Test
    void pwd() {
        CommandLineInterpreter CLI = new CommandLineInterpreter();
        String res = CLI.pwd();
        assertEquals(res, "/home/seifsallam/Java_Projects/Java_CLI");
    }

    @Test
    void cd() {
        CommandLineInterpreter CLI = new CommandLineInterpreter();
        CLI.mkdir(List.of("testDir"));
        boolean test1 = CLI.cd(List.of("testDir"));
        assertEquals(CLI.pwd(), "/home/seifsallam/Java_Projects/Java_CLI/testDir");
        assertTrue(test1);
        CLI.cd(List.of(".."));
        CLI.rmdir(List.of("testDir"));
    }

    @Test
    void testExecuteLsNoArguments() {
        cli.touch(List.of("file1", "file2", "file3", ".hiddenFile")); // Add a hidden file

        String[] result = cli.executeLs(List.of()); // No arguments
        String[] expectedOutput = {
                "file1",
                "file2",
                "file3",
        };
        assertArrayEquals(expectedOutput, result);
    }

    @Test
    void testExecuteLsWithAOption() {
        cli.touch(List.of("file1", "file2", "file3", ".hiddenFile")); // Add a hidden file

        String[] result = cli.executeLs(List.of("-a")); // With -a option
        String[] expectedOutput = {
                ".hiddenFile",
                "file1",
                "file2",
                "file3",
        };
        assertArrayEquals(expectedOutput, result);
    }

    @Test
    void testExecuteLsWithROption() {
        cli.touch(List.of("file1", "file2", "file3", ".hiddenFile")); // Add a hidden file

        String[] result = cli.executeLs(List.of("-r")); // With -r option
        String[] expectedOutput = {
                "file3",
                "file2",
                "file1" // Ensure the order is reversed
        };
        assertArrayEquals(expectedOutput, result);
    }

    @Test
    void testExecuteLsWithAROptions() {
        cli.touch(List.of("file1", "file2", "file3", ".hiddenFile")); // Add a hidden file

        String[] result = cli.executeLs(List.of("-a", "-r")); // With -a and -r options
        String[] expectedOutput = {
                "file3",
                "file2",
                "file1",
                ".hiddenFile",
        };
        assertArrayEquals(expectedOutput, result);
    }

    @Test
    void testMkdir() {
        // Call the mkdir method to create a new directory named "Test"
        boolean isCreated = cli.mkdir(List.of("Test"));
        assertTrue(isCreated, "The directory 'Test' should have been created successfully.");

        // Check the contents of the current directory
        String[] res = cli.executeLs(List.of());

        // Expected output should include "Test"
        String[] expectedOutput = {"Test"};

        // Use assertArrayEquals to compare the actual result with the expected output
        assertArrayEquals(expectedOutput, res, "The directory 'Test' should be listed in the current directory.");
    }

    @Test
    void rmdir() {
        // Call the mkdir method to create a new directory named "Test"
        boolean isCreated = cli.mkdir(List.of("Test"));
        assertTrue(isCreated, "The directory 'Test' should have been created successfully.");

        // Check the contents of the current directory
        boolean res = cli.rmdir(List.of("Test"));
        boolean res2 = cli.rmdir(List.of("Non Existent"));

        assertTrue(res);
        assertFalse(res2);
    }

    @Test
    void touch() {
        // Call the touch method to create a new directory named "Test"
        boolean isCreated = cli.touch(List.of("Test.txt"));
        assertTrue(isCreated, "The directory 'Test' should have been created successfully.");

        // Check the contents of the current directory
        String[] res = cli.executeLs(List.of());

        // Expected output should include "Test"
        String[] expectedOutput = {"Test.txt"};

        // Use assertArrayEquals to compare the actual result with the expected output
        assertArrayEquals(expectedOutput, res, "The directory 'Test' should be listed in the current directory.");
    }

    @Test
    void mv() {
        cli.touch(List.of("Test"));
        cli.mkdir(List.of("Dest"));

        assertArrayEquals(cli.executeLs(List.of()), new String[]{"Dest", "Test"});

        assertTrue(cli.mv(List.of("Test", "Test2")));
        assertArrayEquals(cli.executeLs(List.of()), new String[]{"Dest", "Test2"});

        assertFalse(cli.mv(List.of("Test10", "Test20")));

        assertTrue(cli.mv(List.of("Test2", "Dest")));


        assertArrayEquals(cli.executeLs(List.of()), new String[]{"Dest"});

        cli.cd(List.of("Dest"));

        assertArrayEquals(cli.executeLs(List.of()), new String[]{"Test2"});

        cli.rm(List.of("Test2"));
        cli.cd(List.of(".."));
    }

    @Test
    void rm() {
        cli.touch(List.of("Test"));
        assertArrayEquals(cli.executeLs(List.of()), new String[]{"Test"});

        assertTrue(cli.rm(List.of("Test")));
        assertFalse(cli.rm(List.of("Test100")));
        assertArrayEquals(cli.executeLs(List.of()), new String[]{});
    }

    @Test
    void executeCat() {
        // Create a file called "Test"
        cli.touch(List.of("Test"));
        // Write some content to the "Test" file
        String contentToWrite = "This is a test file.";
        try (FileWriter writer = new FileWriter("testDir/Test")) {
            writer.write(contentToWrite);
        } catch (IOException e) {
            fail("Failed to write to file: " + e.getMessage());
        }

        // Now call the executeCat method
        String[] result = cli.executeCat(List.of("Test"));

        // Expected output should match the content we wrote to the file
        String[] expectedOutput = { "This is a test file." };

        System.out.println(Arrays.toString(result));
        // Assert that the result matches the expected output
        assertArrayEquals(expectedOutput, result);

        // Optionally, clean up by deleting the file after the test
        File fileToDelete = new File("Test");
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    @Test
    void redirectOutput() {
        cli.touch(List.of("Test1", "Test2", "Test3"));
        cli.executeLs(List.of(">", "Test4"));

        assertArrayEquals(cli.executeLs(List.of()), new String[]{
                "Test1",
                "Test2",
                "Test3",
                "Test4"
        });

        assertArrayEquals(cli.executeCat(List.of("Test4")), new String[]{
                "Test1",
                "Test2",
                "Test3"
        });
    }

    @Test
    void appendOutput() {
        cli.touch(List.of("Test1", "Test2", "Test3"));
        cli.executeLs(List.of(">>", "Test4"));
        cli.executeLs(List.of(">>", "Test4"));
        assertArrayEquals(cli.executeLs(List.of()), new String[]{
                "Test1",
                "Test2",
                "Test3",
                "Test4"
        });

        assertArrayEquals(cli.executeCat(List.of("Test4")), new String[]{
                "Test1",
                "Test2",
                "Test3",
                "Test1",
                "Test2",
                "Test3",
                "Test4"
        });
    }

    @AfterEach
    void cleanUp() {
        String[] entries = cli.executeLs(List.of("-a"));
        for (String s : entries) {
            File currentFile = new File(cli.pwd(), s);
            currentFile.delete();
        }
        cli.cd(List.of(".."));
        cli.rmdir(List.of("testDir"));
    }
}