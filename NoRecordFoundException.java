package examples.universityerp;

public class NoRecordFoundException extends Exception
{
    public NoRecordFoundException(String value)
    {
        super("No Record found in File: " + value);
    }
}      