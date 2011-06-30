import java.util.*;

class BrenSched
{
    public static ClassTimeComparator _classTimeComparator = new ClassTimeComparator();

    // Load 5 arrays of 5 with the start and end of each class
    public static ClassTime[][] _classes = {
        {
            new ClassTime( "AHSS", "1000", "0101", "M", "8:05", "10:45" ),
            new ClassTime( "AHSS", "1000", "0201", "M", "11:45", "14:25" ),
            new ClassTime( "AHSS", "1000", "0301", "M", "12:40", "15:20" ),
            new ClassTime( "AHSS", "1000", "0401", "W", "13:35", "16:15" ),
            new ClassTime( "AHSS", "1000", "0501", "W", "17:15", "19:55" ),
        }, {
            new ClassTime( "BADM", "1000", "01", "T", "11:45", "14:25" ),
            new ClassTime( "BADM", "1000", "02", "T", "15:25", "18:05" ),
            new ClassTime( "BADM", "1000", "03", "W", "15:25", "18:05" ),
            new ClassTime( "BADM", "1000", "04", "F", "8:05", "10:45" ),
            new ClassTime( "BADM", "1000", "05", "F", "12:40", "15:20" ),
        }, {
            new ClassTime( "BADM", "1010", "01", "M", "15:25", "18:05" ),
            new ClassTime( "BADM", "1010", "02", "M", "17:15", "19:55" ),
            new ClassTime( "BADM", "1010", "03", "T", "8:05", "10:45" ),
            new ClassTime( "BADM", "1010", "04", "R", "10:50", "13:30" ),
            new ClassTime( "BADM", "1010", "05", "R", "14:30", "17:10" ),
        }, {
            new ClassTime( "BADM", "1030", "01", "T", "9:00", "11:40" ),
            new ClassTime( "BADM", "1030", "02", "T", "11:45", "14:25" ),
            new ClassTime( "BADM", "1030", "03", "W", "8:05", "10:45" ),
            new ClassTime( "BADM", "1030", "04", "W", "12:40", "15:20" ),
            new ClassTime( "BADM", "1030", "05", "W", "16:20", "19:00" ),
        }, {
            new ClassTime( "BADM", "1040", "01", "W", "8:05", "10:45" ),
            new ClassTime( "BADM", "1040", "02", "W", "10:50", "13:30" ),
            new ClassTime( "BADM", "1040", "03", "R", "9:00", "11:40" ),
            new ClassTime( "BADM", "1040", "04", "R", "12:40", "15:20" ),
            new ClassTime( "BADM", "1040", "05", "F", "12:40", "15:20" ),
        }
    };


    public static void main( String[] args )
    {
        // Approach: Brute force generation of all possible combinations of classes
        ClassTime[] schedule = new ClassTime[5];

        // Recursively call to generate all possible combinations
        genCourse( 0, schedule );
    }

    static final int MIN_CLASS_TIME = 2  * 60 / 5; // 10:00am
    static void genCourse( int depth, ClassTime[] schedule )
    {
        if ( depth == 5 ) {

            // Sort the schedule
            ClassTime[] tmpSchedule = new ClassTime[5];
            for ( int i = 0; i < 5; i++ ) {
                tmpSchedule[i] = schedule[i];
            }
            Arrays.sort( tmpSchedule, _classTimeComparator );

            // Evaluate
            int[] days = new int[5];
            int score = 0;
            for ( int i = 0; i < 5; i++ ) {
                ClassTime ct = tmpSchedule[i];
                int start = ct._start;
                int end = ct._end;

                // Scoring:
                // - Penalty proportional to the class's start time before 10:00am
                int dayStart = start % 144;
                if ( dayStart < MIN_CLASS_TIME ) {
                    score -= 200;
                }
                
                // - Penalty for each class on a Monday or Friday
                if ( start < 144 ) {
                    score -= 100;
                } else if ( start >= ( 144 * 4 ) ) {
                    score -= 500;
                }
               
                // - Penalty for every inter-class gap that is less than 1 hour
                if ( i > 0 ) {
                    if ( ( start - tmpSchedule[i-1]._end ) < 12 ) {
                        score -= 100;
                    }
                }

                // Record which day this one starts on, for the days-with-more-than-2-classes penalty
                int dayIndex = start / 144;
                days[dayIndex]++;
            }

            // - Penalty if there is a day with more than 2 classes on it
            for ( int i = 0 ; i < 5; i++ ) {
                if ( days[i] > 2 ) {
                    score -= 500;
                }
            }

            System.out.print( score );
            for ( int i = 0; i < 5; i++ ) {
                System.out.print( " " + tmpSchedule[i].toString() );
            }
            System.out.println();

        } else {
            // Iterate over the 5 classes for the course
            for ( int i = 0; i < 5; i++ ) {
                // Make sure no other classes overlap with this one
                ClassTime ct = _classes[depth][i];

                boolean bad = false;
                for ( int j = 0; j < i; j++ ) {
                    if ( !( ct._end < schedule[j]._start ||
                            ct._start > schedule[j]._end ) ) {
                        bad = true;
                        break;
                    }
                }

                if ( !bad ) {
                    // All is ok from an overlap perspective, so proceed to next level
                    schedule[depth] = ct;
                    genCourse( depth + 1, schedule );
                }
            }
        }
    }
}


class ClassTime
{
    String _program;
    String _course;
    String _section;
    String _day;
    String _startTime;
    String _endTime;
    int    _start;
    int    _end;

    // Map of day code to number of 5-minute intervals of that day's start from Mon 8:00am
    static Hashtable<String,Integer> _dayValues = new Hashtable<String,Integer>();

    ClassTime( String program, String course, String section, String day, String startTime, String endTime )
    {
        // Initialize _dayValues;
        // 8:00am -> 8:00 == 12 hours/day * (60 mins/hr / 5 mins/period) = 144 periods/day
        _dayValues.put( "M", new Integer( 0 * 144 ) );
        _dayValues.put( "T", new Integer( 1 * 144 ) );
        _dayValues.put( "W", new Integer( 2 * 144 ) );
        _dayValues.put( "R", new Integer( 3 * 144 ) );
        _dayValues.put( "F", new Integer( 4 * 144 ) );

        _program = program;
        _course = course;
        _section = section;
        _day = day;
        _startTime = startTime;
        _endTime = endTime;

        int dayValue = _dayValues.get( _day );
        _start = parseTime( startTime ) + dayValue;
        _end = parseTime( endTime ) + dayValue;
    }

    public String toString()
    {
        return _day + ":" + _startTime + "-" + _endTime + "(" + _program + "/" + _course + "/" + _section + ")";
    }

    // Returns the interval number (in the range 0..143) corresponding to this start time
    // 0 == 8:00am
    // 143 == 7:55pm
    static int parseTime( String time )
    {
        // Find the ':' and split it into the two components
        int colonIndex = time.indexOf( ":" );
        int hours = Integer.parseInt( time.substring( 0, colonIndex ) );
        int mins = Integer.parseInt( time.substring( colonIndex + 1 ) );
        return ( hours - 8 ) * 12 + mins / 5;
    }
}

class ClassTimeComparator implements Comparator<ClassTime>
{
    public int compare( ClassTime o1, ClassTime o2 )
    {
        return o1._start - o2._start;
    }
}
