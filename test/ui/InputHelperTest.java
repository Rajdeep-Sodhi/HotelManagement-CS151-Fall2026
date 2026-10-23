package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

class InputHelperTest {
    // Tests common input validation and EXIT/EOF handling with scripted text.
    // No field variables; each test creates an InputHelper(new Scanner(script)).

    private static InputHelper helper(String script) {
        return new InputHelper(new Scanner(script));
    }

    /* Verify invalid integers, ranges, dates, money, blank text, and yes/no responses retry
       until valid input arrives. Assert monetary results as doubles with a small delta. */
    @Test
    void malformedInputIsRetried() {
        // Not a number, below range, above range, then valid
        assertEquals(7, helper("abc\n0\n11\n7\n").readInt("Choice: ", 1, 10));

        // Impossible date, wrong format, then valid
        assertEquals(LocalDate.of(2026, 10, 22),
                helper("2026-02-30\n10/22/2026\n2026-10-22\n").readDate("Date: "));

        // Not a number, negative, NaN, infinity, rounds to zero, then valid
        assertEquals(12.50,
                helper("abc\n-1\nNaN\nInfinity\n0.001\n12.50\n").readMoney("Amount: ", false), 0.001);

        // Extra decimal places are rounded to cents
        assertEquals(12.35, helper("12.349\n").readMoney("Amount: ", false), 0.001);

        // Zero is accepted only when allowZero is true
        assertEquals(0.0, helper("0\n").readMoney("Amount: ", true), 0.001);

        // Blank lines are retried
        assertEquals("Kristy", helper("\n   \nKristy\n").readRequiredText("Name: "));

        // Anything other than yes/no is retried
        assertTrue(helper("maybe\nYES\n").readYesNo("Continue? "));
        assertFalse(helper("1\nn\n").readYesNo("Continue? "));
    }

    /* Check trimmed, case-insensitive EXIT through every input method. */
    @Test
    void exitWorksAtEveryPromptType() {
        String exit = "  eXiT  \n";

        assertThrows(ExitRequestedException.class, () -> helper(exit).readLine("Line: "));
        assertThrows(ExitRequestedException.class, () -> helper(exit).readInt("Choice: ", 1, 10));
        assertThrows(ExitRequestedException.class, () -> helper(exit).readDate("Date: "));
        assertThrows(ExitRequestedException.class, () -> helper(exit).readMoney("Amount: ", false));
        assertThrows(ExitRequestedException.class, () -> helper(exit).readRequiredText("Name: "));
        assertThrows(ExitRequestedException.class, () -> helper(exit).readYesNo("Continue? "));

        // EXIT also works after an invalid answer
        assertThrows(ExitRequestedException.class, () -> helper("abc\nEXIT\n").readInt("Choice: ", 1, 10));
    }

    /* Let the scripted Scanner reach EOF and verify ExitRequestedException reaches the caller. */
    @Test
    void eofEndsInputNormally() {
        // No input at all
        assertThrows(ExitRequestedException.class, () -> helper("").readLine("Line: "));

        // Input runs out while still retrying an invalid answer
        assertThrows(ExitRequestedException.class, () -> helper("abc\n").readInt("Choice: ", 1, 10));
        assertThrows(ExitRequestedException.class, () -> helper("   \n").readRequiredText("Name: "));
    }

    /* Verify readLine preserves non-EXIT text while readRequiredText performs its own
       trimming. */
    @Test
    void rawLinePreservesPasswordWhitespace() {
        assertEquals("  my pass  ", helper("  my pass  \n").readLine("Password: "));
        assertEquals("my name", helper("  my name  \n").readRequiredText("Name: "));

        // A word that only contains "exit" is normal input
        assertEquals("exit123", helper("exit123\n").readLine("Password: "));
    }
}
