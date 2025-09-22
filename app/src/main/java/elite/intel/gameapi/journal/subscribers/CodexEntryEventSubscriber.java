package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CodexEntryEvent;

public class CodexEntryEventSubscriber {

    @Subscribe
    public void onCodexEntryEvent(CodexEntryEvent event) {
        if(event.isNewEntry()) {
            StringBuilder sb = new StringBuilder();
            sb.append("New Codex Entry: ");
            sb.append("Category: ");
            sb.append(event.getCategoryLocalised());
            sb.append(", ");
            sb.append("Name: ");
            sb.append(event.getNameLocalised());
            sb.append(", ");
            sb.append("Voucher Amount: ");
            sb.append(event.getVoucherAmount());
            sb.append(" credits.");
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        }
    }


/*
{
  "timestamp": "2025-09-22T07:08:43Z",
  "event": "CodexEntry",
  "EntryID": 1400159,
  "Name": "$Codex_Ent_IceFumarole_CarbonDioxideGeysers_Name;",
  "Name_Localised": "Carbon Dioxide Ice Fumarole",
  "SubCategory": "$Codex_SubCategory_Geology_and_Anomalies;",
  "SubCategory_Localised": "Geology and anomalies",
  "Category": "$Codex_Category_Biology;",
  "Category_Localised": "Biological and Geological",
  "Region": "$Codex_RegionName_18;",
  "Region_Localised": "Inner Orion Spur",
  "System": "Synuefe LB-W b47-2",
  "SystemAddress": 5073831339417,
  "BodyID": 18,
  "Latitude": -23.575468,
  "Longitude": 2.986898,
  "IsNewEntry": true,
  "VoucherAmount": 50000
}

*/
}
