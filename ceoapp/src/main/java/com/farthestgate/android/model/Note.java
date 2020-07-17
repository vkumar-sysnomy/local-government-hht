package com.farthestgate.android.model;

import com.farthestgate.android.model.database.NotesTable;

/**
 * Created by Hanson Aboagye on 25/04/2014.
 *
 * Class created to facilitate pagination of notes
 *
 */

//TODO: Extend parcelable
public class Note
{
    public String subjectLine;
    public String registration;
    public Boolean isAdHoc;
    public long     dateRecorded;
    public String   ceoNumber;
    public String   pcnNumber;
    public Integer  page;
    public Integer  observation;
    public String  noteText;


    public Note()
    {
        pcnNumber = "";
        isAdHoc = false;
        noteText = "";
    }


    public Note(NotesTable note)
    {
        super();

    }
}
