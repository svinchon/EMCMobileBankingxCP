package ing.rbi.poc;

/**
 * Created by tejas on 27/04/15.
 */
public class BatchUtility {

    public static void getBatchDetails(country_list createFragment) {
        AsyncCreateBatch NewBatch = new AsyncCreateBatch(createFragment.getActivity());
        NewBatch.ticket = createFragment.getFromIntentData(Constants.TICKET);
        NewBatch.FlowSelected = createFragment.getFromIntentData(Constants.SELECTED_FLOW);
        NewBatch.URI = createFragment.getFromIntentData(Constants.URI);
        NewBatch.execute();
        //We need to wait for a bit for this to complete
        while (!NewBatch.Completed == true){
            try{Thread.sleep(100);}
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        if(NewBatch.BatchID == null){
            AlertUtility.ShowAlertDialog(NewBatch.dlgTitle, NewBatch.dlgMessage, createFragment.getActivity());
        }
        else {
            createFragment.addToIntentData(Constants.BATCH_ID, NewBatch.BatchID);
            createFragment.addToIntentData(Constants.BATCH_NAME, NewBatch.BatchName);
        }
    }
}
