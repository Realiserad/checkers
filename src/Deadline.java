import java.util.Date;

public class Deadline {
    private Date mDate;

    public Deadline(Date pDate) {
        mDate = pDate;
    }

    long timeUntil() {
        return mDate.getTime() - (new Date()).getTime();
    }
}
