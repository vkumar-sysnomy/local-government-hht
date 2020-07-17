package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.farthestgate.android.model.Note;

/**
 * Created by Hanson Aboagye on 27/04/2014.
 */

@Table(name = "NotesTable")
public class NotesTable extends Model
{
    public static final String COL_NOTE_SUB      = "SubjectLine";
    public static final String COL_NOTE_CEO      = "CEO_Number";
    public static final String COL_NOTE_PCN_NUM  = "PCNNumber";
    public static final String COL_NOTE_DATE     = "NoteDate";
    public static final String COL_NOTE_OBS      = "Observation";
    public static final String COL_NOTE_PAGE     = "Page";
    public static final String COL_NOTE_TEXT      = "NoteText";
    public static final String COL_NOTE_FILE_NAME = "FileName";

    @Column(name = COL_NOTE_SUB)
    private String subjectLine;

    @Column(name = COL_NOTE_CEO)
    private String ceoNumber;

    @Column(name = COL_NOTE_PCN_NUM)
    private String pcnNumber;

    @Column(name = COL_NOTE_DATE)
    private Long noteDate;

    @Column(name = COL_NOTE_TEXT)
    private String noteText;

    @Column(name = COL_NOTE_OBS)
    private Integer observation;

    @Column(name = COL_NOTE_PAGE)
    private Integer page;


    @Column(name = COL_NOTE_FILE_NAME)
    private String fileName;


    public NotesTable() { super();}

    public NotesTable(Note noteInfo)
    {
        super();
        subjectLine = noteInfo.registration;
        ceoNumber           = noteInfo.ceoNumber;
        noteDate            = noteInfo.dateRecorded;
        if (noteInfo.isAdHoc)
            pcnNumber       = "0";
        else
            pcnNumber       = noteInfo.pcnNumber;
        observation         = noteInfo.observation;
        page                = noteInfo.page;
        subjectLine         = noteInfo.subjectLine;
        noteText            = noteInfo.noteText;
    }

    public String getSubjectLine() {
        return subjectLine;
    }

    public void setSubjectLine(String subjectLine) {
        this.subjectLine = subjectLine;
    }

    public Integer getObservation() {
        return observation;
    }

    public void setObservation(Integer observation) {
        this.observation = observation;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPage() {
        return page;
    }

    public String getCeoNumber() {
        return ceoNumber;
    }

    public void setCeoNumber(String ceoNumber) {
        this.ceoNumber = ceoNumber;
    }

    public String getPcnNumber() {
        return pcnNumber;
    }

    public void setPcnNumber(String pcnNumber) {
        this.pcnNumber = pcnNumber;
    }

    public Long getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(Long noteDate) {
        this.noteDate = noteDate;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
}
