package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hanson ABoagye on 04/2014.
 */
@Table(name = "TicketNoTable")
public class TicketNoTable extends Model{

    public static final String COL_TICKET_REF = "TicketReference";
    public static final String COL_DATE_USED = "DateUsed";
    public static final String COL_TICKET_NODE = "TicketRef";

    @Column(name = COL_TICKET_REF)
    private String ticketReference;

    @Column(name = COL_DATE_USED)
    private String dateUsed;

    @Column(name = COL_TICKET_NODE)
    private String ticketNode;

    public TicketNoTable(){ super(); }

    public String getTicketReference() {
        return ticketReference;
    }

    public void setTicketReference(String ticketRef) {
        this.ticketReference = ticketRef;
    }

    public String getDateUsed() { return dateUsed;  }

    public void setDateUsed(String setDate) {
        this.dateUsed = setDate;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("ticketNode","");
        res.put("ticketReference",ticketReference);
        res.put("dataUsed", dateUsed);

        return res;
    }
}
