package ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputHelper{
                // Reads every input through one common EXIT/EOF-aware method.
                private final Scanner scanner; // The Scanner shared by this application's menus.

                /* Store the provided Scanner. Main passes its single System.in Scanner; tests pass
                   a Scanner created from a multiline String. */
                public InputHelper(Scanner scanner)
                {
                    if (scanner == null)
                    {
                        throw new IllegalArgumentException("Scanner cannot be null.");
                    }
                    this.scanner = scanner;
                }

                /* Read through the common input path. Trim/case-match EXIT before parsing and treat EOF
                   as normal exit. Preserve the original returned text so passwords are not trimmed. */
                public String readLine(String prompt)
                {
                    System.out.print(prompt);
                    if (!scanner.hasNextLine())
                    {
                        throw new ExitRequestException();
                    }
                    String line = scanner.nextLine();
                    if (line.trim().equalsIgnoreCase("EXIT"))
                    {
                        throw new ExitRequestException();
                    }
                    return line;
                }

                /* Retry until a whole number within the inclusive bounds is entered; explain invalid
                   input. */
                public int readInt(String prompt, int minimum, int maximum)
                {
                    while (true)
                    {
                        String text = readLine(prompt).trim();
                        try
                        {
                            int value = Integer.parseInt(text);
                            if (value >= minimum && value <= maximum)
                            {
                                return value;
                            }

                            System.out.println("Enter anumber from " + minimum + " to " + maximum + ".");

                        } catch (NumberFormatException e)
                        {
                            System.out.println("Enter a whole number.");
                        }
                    }
                }

                /* Retry until a valid ISO date in yyyy-MM-dd format is entered. */
                public LocalDate readDate(String prompt)
                {
                    while (true)
                    {
                        String text = readLine(prompt).trim();
                        try
                        {
                            return LocalDate.parse(text);
                        }
                        catch (DateTimeParseException e)
                        {
                            System.out.println("Enter a valid date as YYYY-MM-DD.");
                        }
                    }
                }

                /* Retry until a finite nonnegative double is entered, round it to cents, and reject
                   zero after rounding unless allowZero is true. */
                public double readMoney(String prompt, boolean allowZero)
                {
                    while (true)
                    {
                        String text = readLine(prompt).trim();
                        double value;
                        try
                        {
                            value = Double.parseDouble(text);

                        }
                        catch (NumberFormatException e)
                        {
                            System.out.println("Enter an amount such as 25 or 25.50.");
                            continue;
                        }
                        double rounded = Math.round(value * 100.0) / 100.0;
                        if (rounded == 0.0 && !allowZero)
                        {
                            System.out.println("The amount must be greater than zero.");
                            continue;
                        }
                        return rounded;
                    }
                }

                /* Trim and retry until nonblank text is entered. */
                public String readRequiredText(String prompt)
                {
                    while (true)
                    {
                        String text = readLine(prompt).trim();
                        if (!text.isEmpty())
                        {
                            return text;
                        }
                        System.out.println("This field cannot be blank.");
                    }
                }

                /* Accept a case-insensitive yes/no answer; explain invalid input and retry. */
                public boolean readYesNo(String prompt)
                {
                    while (true)
                    {
                        String text = readLine(prompt).trim().toLowerCase();
                        if (text.equals("y") || text.equals("yes"))
                        {
                            return true;
                        }
                        if (text.equals("n") || text.equals("no"))
                        {
                            return false;
                        }
                        System.out.println("Enter yes or no.");
                    }
                }

                /* Describe the input helper configuration; omit supplied input. */
                @Override
                public String toString()
                {
                    return "InputHelper{reads from a shared Scanner; EXIT or end of input stops the program}";
                }
}

