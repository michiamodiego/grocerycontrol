package com.ds.app.pricereading.activity.support;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ds.app.pricereading.R;

import java.util.ArrayList;
import java.util.List;

public class ResultViewer extends LinearLayout {
    // TODO to add ContextMenu

    public ResultViewer(Context context) {
        super(context);
    }

    public ResultViewer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public ResultViewer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {

        pageSizeFormat = attrs.getAttributeValue(null, "page_size_format");
        pageNumberFormat = attrs.getAttributeValue(null, "page_number_format");
        pageSizeList = convertToIntegerList(attrs.getAttributeValue(null, "page_size_list").split(","));
        pageDefaultSize = Integer.parseInt(attrs.getAttributeValue(null, "page_default_size"));
        defaultPageSizeIndex = pageSizeList.indexOf(pageDefaultSize);

        if (defaultPageSizeIndex == -1) {
            throw new IllegalArgumentException();
        }

        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(
                        R.layout.result_viewer_layout,
                        this,
                        true
                );

        setOrientation(LinearLayout.VERTICAL);

        previousButton = findViewById(R.id.result_viewer_previous_page_button);
        settingsButton = findViewById(R.id.result_viewer_settings_button);
        pageNumberSpinner = findViewById(R.id.result_viewer_page_number_spinner);
        nextButton = findViewById(R.id.result_viewer_next_page_button);
        recyclerView = findViewById(R.id.result_viewer_recycler_view);

        previousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPage(currentPage - 1, currentPageSize);
            }
        });

        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog
                        .Builder(getContext())
                        .setAdapter(
                                createPageSizeSpinnerAdapter(
                                        context,
                                        createPageSizeLabelList(
                                                pageSizeFormat,
                                                pageSizeList
                                        )
                                ),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPage(1, pageSizeList.get(which));
                                        dialog.dismiss();
                                    }
                                })
                        .create()
                        .show();
            }
        });

        pageNumberSpinner.setAdapter(createPageNumberSpinnerAdapter(context, new ArrayList<>()));
        pageNumberSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedPage = pageNumberList.get(position);
                if (selectedPage != currentPage) {
                    requestPage(selectedPage, currentPageSize);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        recyclerView.setAdapter(createRecyclerViewAdapter(pageStateSupport));
        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                )
        );

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPage(currentPage + 1, currentPageSize);
            }
        });

    }

    private static List<String> createPageSizeLabelList(
            final String pageSizeFormat,
            final List<Integer> pageSizeList
    ) {
        final ArrayList<String> pageSizeLabelList = new ArrayList<>();
        for (Integer t : pageSizeList) {
            pageSizeLabelList.add(String.format(pageSizeFormat, t.intValue()));
        }
        return pageSizeLabelList;
    }

    private static List<Integer> convertToIntegerList(final String[] stringifiedPageSizeList) {
        final ArrayList<Integer> pageSizeList = new ArrayList<>();
        for (int i = 0; i < stringifiedPageSizeList.length; i++) {
            pageSizeList.add(
                    Integer.parseInt(
                            stringifiedPageSizeList[i].trim()
                    )
            );
        }
        return pageSizeList;
    }

    private void setPage(
            int dataSize,
            int page,
            int pageSize,
            int itemLayout,
            ItemViewBinderCallback itemViewBinderCallback
    ) {

        pageStateSupport.setDataSize(dataSize);
        pageStateSupport.setItemLayout(itemLayout);
        pageStateSupport.setItemViewBinderCallback(itemViewBinderCallback);

        if (!(page >= 1 && page <= getLastPage(dataSize, pageSize))) {
            throw new IllegalArgumentException();
        }

        if (pageSizeList.indexOf(pageSize) == -1) {
            throw new IllegalArgumentException();
        }

        previousButton.setEnabled(true);
        nextButton.setEnabled(true);

        if (page == 1) {
            previousButton.setEnabled(false);
        }

        if (page == getLastPage(dataSize, pageSize)) {
            nextButton.setEnabled(false);
        }

        pageSizeSpinnerInitialized = 0;

        currentPage = page;
        currentPageSize = pageSize;
        pageNumberSpinnerInitialized = 0;

        pageNumberSpinner.setAdapter(
                createPageNumberSpinnerAdapter(
                        getContext(),
                        pageNumberLabelList = createPageNumberLabelList(
                                pageNumberFormat,
                                pageNumberList = createPageNumberList()
                        )
                )
        );
        pageNumberSpinner.setSelection(pageNumberList.indexOf(currentPage));

        recyclerView
                .getAdapter()
                .notifyDataSetChanged();

    }

    private List<String> createPageNumberLabelList(
            String pageNumberFormat,
            List<Integer> integers
    ) {
        ArrayList<String> pageNumberLabelList = new ArrayList<>();
        int lastPage = getLastPage(pageStateSupport.getDataSize(), currentPageSize);
        for (int i = 1; i <= lastPage; i++) {
            pageNumberLabelList.add(
                    String.format(pageNumberFormat, i, lastPage)
            );
        }
        return pageNumberLabelList;
    }

    private List<Integer> createPageNumberList() {
        final ArrayList<Integer> numberList = new ArrayList<>();
        for (int i = 0; i < getLastPage(pageStateSupport.getDataSize(), currentPageSize); i++) {
            numberList.add(i + 1);
        }
        return numberList;
    }

    public void setOnPageRequestedListener(OnPageChangedListener onPageChangedListener) {
        this.onPageChangedListener = onPageChangedListener;
        requestPage(1, pageDefaultSize);
    }

    public int getLastPage(int dataSize, int pageSize) {
        return dataSize != 0 ? new Double(
                Math.ceil(((double) dataSize) / ((double) pageSize)))
                .intValue() : 1;
    }

    private void requestPage(int requestedPage, int requestedPageSize) {
        invokeOnPageChangedListener(requestedPage, requestedPageSize);
    }

    private void invokeOnPageChangedListener(int page, int pageSize) {
        if (onPageChangedListener != null) {
            onPageChangedListener.invoke(
                    new ResultViewerPageTransaction(page, pageSize, ResultViewer.this),
                    getNextStartIndex(),
                    getNextEndIndex()
            );
        }
    }

    public int getNextStartIndex() {
        return (currentPage - 1) * currentPageSize;
    }

    public int getNextEndIndex() {
        return getNextStartIndex() + Math.min(currentPageSize,
                pageStateSupport.getDataSize() - (currentPage - 1) * currentPageSize);
    }

    private PageStateSupport pageStateSupport = new PageStateSupport();
    private OnPageChangedListener onPageChangedListener;

    private String pageSizeFormat;
    private String pageNumberFormat;
    private List<Integer> pageSizeList;
    private Integer pageDefaultSize;
    private int defaultPageSizeIndex;
    private int pageSizeSpinnerInitialized = 0;
    private int currentPageSize;
    private int currentPage;
    private List<Integer> pageNumberList;
    private List<String> pageNumberLabelList;
    private int pageNumberSpinnerInitialized = 0;

    private ImageButton previousButton;
    private ImageButton settingsButton;
    private Spinner pageSizeSpinner;
    private Spinner pageNumberSpinner;
    private ImageButton nextButton;
    private RecyclerView recyclerView;

    public void forceRefresh() {
        requestPage(currentPage, currentPageSize);
    }

    public static interface ItemViewBinderCallback {
        void invoke(View view, int position);
    }

    public static interface ContextMenuHandler {
        void handle(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, int position);
    }

    public static interface OnPageChangedListener {
        void invoke(ResultViewerPageTransaction pageTransaction, int startIndex, int endIndex);
    }

    public static class ResultViewerViewHolder extends RecyclerView.ViewHolder {

        public ResultViewerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
        }

        public View getView() {
            return view;
        }

        private View view;

    }

    public static class ResultViewerPageTransaction {

        public int getOffset() {
            return (getPage() - 1) * getPageSize();
        }

        public int getCount() {
            return getPageSize();
        }

        public int getPage() {
            return page;
        }

        public int getPageSize() {
            return pageSize;
        }

        public ResultViewerPageTransaction(
                int page,
                int pageSize,
                ResultViewer resultViewer
        ) {
            this.page = page;
            this.pageSize = pageSize;
            this.resultViewer = resultViewer;
        }

        public void commit(
                int dataSize,
                int itemLayout, ItemViewBinderCallback itemViewBinderCallback) {
            resultViewer.setPage(dataSize, page, pageSize, itemLayout, itemViewBinderCallback);
            if (dataSize == 0) {
                resultViewer.showNoItemPage();
            } else {
                resultViewer.showResultPage();
            }
        }

        private final int page;
        private final int pageSize;
        private final ResultViewer resultViewer;

    }

    private void showResultPage() {
        findViewById(R.id.result_viewer_textview_layout)
                .setVisibility(View.GONE);
        findViewById(R.id.result_viewer_resultviewer_layout)
                .setVisibility(View.VISIBLE);
    }

    private void showNoItemPage() {
        findViewById(R.id.result_viewer_textview_layout)
                .setVisibility(View.VISIBLE);
        findViewById(R.id.result_viewer_resultviewer_layout)
                .setVisibility(View.GONE);
    }

    private static ArrayAdapter<String> createPageSizeSpinnerAdapter(
            Context context,
            List<String> pageSizeLabelList
    ) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.result_viewer_page_size_spinner_layout);
        adapter.addAll(pageSizeLabelList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private static ArrayAdapter<String> createPageNumberSpinnerAdapter(
            Context context,
            List<String> pageNumberLabelList
    ) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.result_viewer_page_number_spinner_layout);
        adapter.addAll(pageNumberLabelList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private static RecyclerView.Adapter createRecyclerViewAdapter(PageStateSupport pageStateSupport) {

        final RecyclerView.Adapter adapter = new RecyclerView.Adapter() {

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ResultViewerViewHolder(
                        LayoutInflater
                                .from(parent.getContext())
                                .inflate(
                                        pageStateSupport.getItemLayout(),
                                        parent,
                                        false
                                )
                );
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                pageStateSupport
                        .getItemViewBinderCallback()
                        .invoke(((ResultViewerViewHolder) holder).getView(), position);
            }

            @Override
            public int getItemCount() {
                return pageStateSupport.getItemCount();
            }

        };

        return adapter;

    }

    private class PageStateSupport {

        public int getItemLayout() {
            return itemLayout;
        }

        public void setItemLayout(int itemLayout) {
            this.itemLayout = itemLayout;
        }

        public int getDataSize() {
            return dataSize;
        }

        public int getItemCount() {
            return dataSize != 0 ?
                    Math.min(dataSize - (currentPage - 1) * currentPageSize, currentPageSize) :
                    0;
        }

        public void setDataSize(int dataSize) {
            this.dataSize = dataSize;
        }

        public ContextMenuHandler getContextMenuHandler() {
            return this.contextMenuHandler;
        }

        public void setContextMenuHandler(ContextMenuHandler contextMenuHandler) {
            this.contextMenuHandler = contextMenuHandler;
        }

        public ItemViewBinderCallback getItemViewBinderCallback() {
            return itemViewBinderCallback;
        }

        public void setItemViewBinderCallback(ItemViewBinderCallback itemViewBinderCallback) {
            this.itemViewBinderCallback = itemViewBinderCallback;
        }

        private int itemLayout;
        private int dataSize;
        private ContextMenuHandler contextMenuHandler;
        private ItemViewBinderCallback itemViewBinderCallback;

    }

}