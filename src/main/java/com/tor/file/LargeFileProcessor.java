package com.tor.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

public class LargeFileProcessor {

    // \\P denotes POSIX expression and L denotes character class for word characters
    private static final String REGEX_FOR_WORDS = "\\P{L}+";
    // 1GB text file, takes around 1 minutes for processing
    private static final String FILE_PATH_FOR_SORTING = "./src/main/resources/data/freecodecamp_casual_chatroom_anon.csv";
    // 2.6GB text file, takes around 2 minutes for processing
    //private static final String FILE_PATH_FOR_SORTING = "./src/main/resources/data/freecodecamp_casual_chatroom.csv"; //2.6 GB text file
    private static final String OUTPUT_FILE_PATH_FOR_SORTING = "output_sorted.txt";
    private static final long chunkFactor = 100;
    private static final String tempFile = "temp_file_";
    private static File tmpDir = null;
    private static File outputDir = null;

    public static void fileProcessor(File filename) throws IOException {

        tmpDir = new File("./src/main/resources/temp");
        outputDir = new File("./src/main/resources/output");

        // if the directory does not exist, create it
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }

        // if the directory does not exist, create it
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // Determining the chunk size in bytes taking chunkFactor
        // to give enough memory space to other java threads
        long availableMemory = getTotalAvailableMemoryInCurrentSystem();
        long CHUNK_SIZE = availableMemory / chunkFactor;
        //long CHUNK_SIZE = 10000;

        System.out.println("Available memory in system in bytes: " + availableMemory);
        System.out.println("Chunk size in bytes: " + CHUNK_SIZE);

        // determine file size
        long FILE_SIZE = 0;
        if (filename.exists())
            FILE_SIZE = filename.length();
        else
            throw new FileNotFoundException("File passed not found");

        System.out.println("Input File size in bytes :  " + FILE_SIZE);
        int Number_of_chunks = (int) Math.ceil((double)FILE_SIZE / CHUNK_SIZE);
        System.out.println("Splitting the file into : [" + Number_of_chunks + "] chunks ");

        // Split the original file into different chunks and save to
        // disk removing duplicates and in sorted order
        System.out.println("Starting file splitting..");
        fileSplitter(filename, CHUNK_SIZE, Number_of_chunks);
        System.out.println("Completed writing sorted and distinct words into " + Number_of_chunks + " file chunks ");

        // Merges already saved temporary files to make a single file
        // in a sorted order
        System.out.println("Starting sorted temporary file merging..");
        StringMerger( Number_of_chunks);
        System.out.println("Merged the temporary files into a single sorted file containing distinct words");
        System.out.println("File location -"+outputDir+"/"+OUTPUT_FILE_PATH_FOR_SORTING);
    }

    // 1. reads the file original file source
    // 2. split into different files of chunk size
    // 3. removing duplicates and in sorted order
    private static void fileSplitter(File filename, long CHUNK_SIZE, int number_of_chunks) throws FileNotFoundException {
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);
        FileWriter writer = null;
        BufferedWriter bufferedWriter = null;

        for (int i = 0; i < number_of_chunks; i++) {

            try {
                StringBuilder sb = new StringBuilder();
                String line;

                while (sb.length() < CHUNK_SIZE) {
                    if ((line = br.readLine()) != null) {

                        sb.append(line);
                        //System.out.println("here "+sb.length() +" : "+CHUNK_SIZE);
                    } else {
                        break;
                    }
                }

                String trimmedFileLine = sb.toString().trim();
                //1. Splitting the string to  obtain only words
                //2. Removing duplicates by adding to a Set
                //3. Using a Treeset with no comparator since we want the natural ordering resulting in sorted words
                final TreeSet<String> uniqueWords = new TreeSet<String>(Arrays.asList(trimmedFileLine.split(REGEX_FOR_WORDS)));

                //Freeing up the memory of the huge string
                sb = null;
                trimmedFileLine = null;

                //Write down the sorted distinct words into secondary memory
                writer = new FileWriter(tmpDir+"/"+tempFile + Integer.toString(i) + ".txt");
                bufferedWriter = new BufferedWriter(writer);

                Iterator<String> it = uniqueWords.iterator();
                while (it.hasNext()) {
                    bufferedWriter.write(it.next().toString());
                    bufferedWriter.newLine();
                }

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {

            bufferedWriter.close();
            writer.close();
            br.close();
            fr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 1. Merges the strings by reading from  different files
    // 2. Uses min heap to get the smallest string (O(logn))
    // 3. making sure to read the next string from the file from where we found the minimum string
    // 4. Writes the new output to OUTPUT_FILE
    private static void StringMerger( int number_of_chunks) throws IOException {
        FileWriter writer;
        BufferedWriter bufferedWriter;
        BufferedReader[] brs = new BufferedReader[number_of_chunks];
        Heap heap = new Heap();
        writer = new FileWriter(outputDir+"/"+OUTPUT_FILE_PATH_FOR_SORTING);
        bufferedWriter = new BufferedWriter(writer);
        Heap.Node node = null;
        String previousString = StringUtils.EMPTY;

        //initializing the min heap with first values of each temp file
        for (int i = 0; i < number_of_chunks; i++) {

            brs[i] = new BufferedReader(new FileReader(tmpDir+"/"+tempFile + Integer.toString(i) + ".txt"));
            node = heap.new Node(brs[i].readLine(), brs[i]);
            if (StringUtils.isNotBlank(node.getValue())) {
                heap.addToHeap(node);
            }
        }

        // Traversing until all the temp files are read
        while (!heap.isEmpty()) {

            Heap.Node min = heap.getMin();
            if (min != null) {
                String currentString = min.getValue();
                if (!StringUtils.equalsIgnoreCase(currentString, previousString)) {
                    bufferedWriter.write(currentString);
                    bufferedWriter.newLine();
                    previousString = currentString;
                }
                BufferedReader buff = min.getCurBuffer();
                String nextLine = buff.readLine();
                if (nextLine != null) {
                    node = heap.new Node(nextLine, buff);
                    if (StringUtils.isNotBlank(node.getValue())) {
                        heap.addToHeap(node);
                    }
                }
            }
        }

        //Closing all the file handlers
        try {
            bufferedWriter.close();
            writer.close();
            for (int i = 0; i < number_of_chunks; i++)
                brs[i].close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> failedFileNames = new ArrayList<String>();

        try {
            FileUtils.deleteDirectory(tmpDir);
        }
        catch (IOException e){
            System.out.println("Some of the temperory files are not deleted");
        }
    }

    // This function gives back maximum free memory in jvm
    // the return type is measured in bytes
    private static long getTotalAvailableMemoryInCurrentSystem() {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long allocatedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxFreeMemory = runtime.maxMemory() - allocatedMemory;
        return maxFreeMemory;
    }


    public static void main(String[] args) {

        File f = new File(FILE_PATH_FOR_SORTING);
        try {
            fileProcessor(f);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.out.println("The file for processing could not be found at : " + FILE_PATH_FOR_SORTING);
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
