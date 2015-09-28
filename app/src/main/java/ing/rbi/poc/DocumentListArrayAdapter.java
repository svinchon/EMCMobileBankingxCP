package ing.rbi.poc;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by tejas on 28/04/15.
 */
public class DocumentListArrayAdapter extends ArrayAdapter{
    public static final String UNKNOWN = "UNKNOWN";
    static HashMap<String, Integer> contentToImageResourceMap = new HashMap<String, Integer>();

    static {
        contentToImageResourceMap.put("pdf", ing.rbi.poc.R.drawable.i_pdf);
        contentToImageResourceMap.put("jpg", ing.rbi.poc.R.drawable.i_photo);
        contentToImageResourceMap.put("jpeg", ing.rbi.poc.R.drawable.i_photo);
        contentToImageResourceMap.put("doc", ing.rbi.poc.R.drawable.i_doc);
        contentToImageResourceMap.put("png", ing.rbi.poc.R.drawable.i_png);
        contentToImageResourceMap.put(UNKNOWN, ing.rbi.poc.R.drawable.unknown_file);
    }

    //int[] imageResourceArray = new int[]{R.drawable.i_doc, R.drawable.i_jpg,R.drawable.i_pdf, R.drawable.i_png};
    String[] myContentTypes;

     public DocumentListArrayAdapter(Context context, int resource, int textViewResourceId, Object[] objects, String[] contentTypes) {
        super(context, resource, textViewResourceId, objects);
        myContentTypes = contentTypes;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ImageView imageVIew = (ImageView) view.findViewById(ing.rbi.poc.R.id.doc_list_item_icon);
        String contentType = myContentTypes[position];
        int resourceId;
        if(contentToImageResourceMap.containsKey(contentType)) {
            resourceId = contentToImageResourceMap.get(contentType);
        }
        else {
            resourceId = contentToImageResourceMap.get(UNKNOWN);
        }
        imageVIew.setImageResource(resourceId);
        return view;
    }
}
