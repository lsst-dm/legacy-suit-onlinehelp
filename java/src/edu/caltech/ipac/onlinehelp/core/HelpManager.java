package edu.caltech.ipac.onlinehelp.core;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Date: 6/25/12
 *
 * @author loi
 * @version $Id: HelpManager.java,v 1.10 2012/12/10 22:06:47 loi Exp $
 */
public class HelpManager {


    public static final String HELP_DIV = "toc";
    private static final String IRSA_ONLINE_HELP = "http://irsa.ipac.caltech.edu/onlinehelp/";

    private HelpItem root;
    private Tree tree;
    private NamedFrame helpWindow;
    private boolean isInit = false;
    private DockLayoutPanel mainPanel;
    private static final double DEF_WIDTH = 200;
    private ScrollPanel spTree;
    private HorizontalPanel toolbar;
    private static final String HELP_WINDOW = "help-content";
    private String buildDate = "";

    private void init() {
        if (!isInit) {
            buildDate = String.valueOf(Util.getMetaInfo("BUILD-SDATE"));

            root = getToc(HELP_DIV);
            tree = createTree(root);
            helpWindow = new NamedFrame(HELP_WINDOW);

            HTML gbar = getSearchBar();
            HTML title = new HTML("<b>" + Window.getTitle() + "</b>");
            toolbar = new HorizontalPanel();
            toolbar.add(title);
            toolbar.add(gbar);
            toolbar.setWidth("100%");
            Util.setStyle(toolbar, "paddingTop", "5px");
            toolbar.setCellWidth(gbar, "400px");
            toolbar.setCellHorizontalAlignment(gbar, HorizontalPanel.ALIGN_RIGHT);
            toolbar.setCellHorizontalAlignment(title, HorizontalPanel.ALIGN_CENTER);

            // add view pdf is exists
            String pdfUrl = Util.getMetaInfo("pdf-file");
            if (!Util.isEmpty(pdfUrl)) {
                Anchor viewPdf = new Anchor("View PDF", pdfUrl);
                toolbar.insert(viewPdf, 1);
                toolbar.setCellWidth(viewPdf, "100px");
                toolbar.setCellHorizontalAlignment(viewPdf, HorizontalPanel.ALIGN_CENTER);
            }

            spTree = new ScrollPanel(tree);
            Util.setStyle(spTree, "textAlign", "left");
            mainPanel = new DockLayoutPanel(Style.Unit.PX);
            mainPanel.addNorth(toolbar, 30);
            mainPanel.addWest(spTree, DEF_WIDTH);
            mainPanel.add(helpWindow);
            mainPanel.setSize("100%", "100%");
            helpWindow.setSize("100%", "100%");
            Util.setStyle(helpWindow, "backgroundColor", "white");
            tree.addCloseHandler(new CloseHandler() {
                public void onClose(CloseEvent closeEvent) {
                    resizeDockPanel();
                }
            });
            tree.addOpenHandler(new OpenHandler() {
                public void onOpen(OpenEvent openEvent) {
                    resizeDockPanel();
                }
            });

            if (helpWindow.getElement().getParentElement() != null) {
                Util.setStyle(helpWindow, "border", "none");
                Util.setStyles((Element) helpWindow.getElement().getParentElement(), "borderLeft", "2px solid gray", "borderTop", "2px solid gray");
            }
            RootPanel.get(HELP_DIV).add(mainPanel);
            isInit = true;
        }
    }

    private HTML getSearchBar() {
        HTML html = new HTML("<form id='cref' action='http://www.google.com/cse' target='" + HELP_WINDOW + "'>"
                + "<input type='hidden' name='cref' value='" + IRSA_ONLINE_HELP + Util.getMetaInfo("app-name") + "/cse.xml' />"
//                + "<input type='hidden' name='url' value='" + GWT.getModuleBaseURL() + "' />"
                + "<input name='q' type='text' size='40' />"
                + "<input type='submit' name='sa' value='Search' />"
                + "</form>"
                + "<script type='text/javascript' src='http://www.google.com/cse/brand?form=cref'></script>"
                );

        return html;
    }
    
    private HelpItem getToc(String tocDiv) {
        RootPanel root = RootPanel.get(tocDiv);
        Element el = root.getElement();
        NodeList<com.google.gwt.dom.client.Node> children = el.getChildNodes();
        HelpItem helpRoot = new HelpItem("root", "root", "root", null);
        for(int i = 0; i < children.getLength(); i++) {
            Node n = children.getItem(i);
            addAllHelpItem(n, helpRoot);
        }
        root.getElement().setInnerHTML("");
        return helpRoot;
    }

    private void addAllHelpItem(Node n, HelpItem parent) {
        if (parent == null) throw new NullPointerException("Should not happen!!!");
        HelpItem item = parse(n);
        if (parent != null && item != null) {
            parent.addChild(item);
        }
        item = item == null ? parent : item;
        if (n.hasChildNodes()) {
            NodeList<com.google.gwt.dom.client.Node> children = n.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
                addAllHelpItem(children.getItem(i), item);
            }
        }
    }

    private HelpItem parse(Node node) {
        HelpItem newItem = null;
        if (Element.is(node)) {
            com.google.gwt.dom.client.Element el = Element.as(node);
            String tagName = el.getTagName();
            if (!Util.isEmpty(tagName) && tagName.equalsIgnoreCase("li")) {
                String id = el.getId();
                String desc = el.getAttribute("title");
                String style = el.getAttribute("style");
                String url = "";
                String title = "";
                NodeList<com.google.gwt.dom.client.Element> links = el.getElementsByTagName("a");
                if (links != null && links.getLength() > 0) {
                    com.google.gwt.dom.client.Element href = links.getItem(0);
                    url = href.getAttribute("href");
                    title = href.getInnerText();
                }
                newItem = new HelpItem(id, title, desc, url);
                if (!Util.isEmpty(style)) {
                    newItem.setStyleAttribs(style);
                }
            }
        }
        return newItem;
    }

    private void resizeDockPanel() {
        double w = DEF_WIDTH;
        boolean isResized = false;
        while (!isResized) {
            Util.DockLayout.setWidgetChildSize(spTree, w);
            mainPanel.forceLayout();
            isResized = spTree.getMaximumHorizontalScrollPosition() == 0 || w > 400;
            w = w + 20;
        }
    }

    public Widget getDisplay() {
        init();
        return mainPanel;
    }

    public void showHelp() {
        showHelpAt(null);
    }

    public void showHelpAt(final String helpId) {
        deferredShowHelpAt(helpId);
    }

//====================================================================
//
//====================================================================

    private void deferredShowHelpAt(String helpId) {
        init();
        
        helpId = Util.isEmpty(helpId) ? null : helpId.startsWith("HelpMenu.") ? helpId.replace("HelpMenu.", "") : helpId;
        
        TreeItem sItem = null;
        HelpItem hItem = null;
        if (helpId == null) {
            sItem = tree.getItem(0);
        } else {
            for (Iterator<TreeItem> itr = tree.treeItemIterator(); itr.hasNext(); ) {
                TreeItem ti = itr.next();
                if (ti instanceof NodeItem) {
                    HelpItem hi = ((NodeItem) ti).getHelpItem();
                    if (hi.getName() != null && hi.getName().equals(helpId)) {
                        sItem = ti;
                        hItem = hi;
                    }
                }
            }
        }
        if (helpId != null && sItem == null) {
//            Window.alert("Help topic (" + helpId + ") not found");
        } else {
            if (sItem != null) {
                tree.setSelectedItem(sItem, true);
                tree.ensureSelectedItemVisible();
            }
        }
        resizeDockPanel();

    }

    private HelpItem getHelpItem(String helpId) {
        for (HelpItem itm : root.getChildren()) {
            if (itm.getName().equals(helpId)) {
                return itm;
            }
            if (itm.getChildren().size() > 0) {
                return getHelpItem(helpId);
            }
        }
        return null;
    }

    private Tree createTree(HelpItem root) {

        Tree tree = new Tree();
        for (HelpItem hi : root.getChildren()) {
            TreeItem ti = new NodeItem(hi);
            if (hi.getStyleAttribs() != null) {
                Util.setStyles(ti.getElement(), Util.extractStyles(hi.getStyleAttribs()));
            }

            tree.addItem(ti);
            buildTree(hi, ti);
        }


        return tree;
    }

    private void buildTree(HelpItem hi, TreeItem ti) {
        for (HelpItem nhi : hi.getChildren()) {
            TreeItem nti = new NodeItem(nhi);
            if (nhi.getStyleAttribs() != null) {
                Util.setStyles(nti.getElement(), Util.extractStyles(nhi.getStyleAttribs()));
            }
            ti.addItem(nti);
            buildTree(nhi, nti);
        }
    }

//====================================================================
//
//====================================================================

    private class NodeItem extends TreeItem {
        HelpItem hi;

        private NodeItem(HelpItem hi) {
            super(SafeHtmlUtils.fromString(hi.getTitle()));
            this.hi = hi;
            setTitle(hi.getDesc());
        }

        public HelpItem getHelpItem() {
            return hi;
        }
        
        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            if (selected) {
                String url = hi.getUrl();
                if (url.toLowerCase().endsWith("html")) {
                    url = url + "?bd=" + buildDate;
                }
                helpWindow.setUrl(url);
                History.newItem("id=" + hi.getName(), false);
            }
        }

    }


    public static class HelpItem {

        private String name;
        private String title;
        private String desc;
        private String url;
        private List<HelpItem> children;
        private HelpItem parent;
        private String styleAttribs;

        public HelpItem(String name) {
            this(name, name, name, null);
        }

        public HelpItem(String name, String title, String desc, String url) {
            if (name == null) {
                throw new NullPointerException("name may not be null");
            }
            this.name = name;
            this.title = title;
            this.desc = desc;
            this.url = url;
        }

        public void addChild(HelpItem item) {
            if (children == null) {
                children = new ArrayList<HelpItem>();
            }
            children.add(item);
        }

        public List<HelpItem> getChildren() {
            if (children != null) {
                return Collections.unmodifiableList(children);
            }
            return new ArrayList<HelpItem>(0);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public HelpItem getParent() {
            return parent;
        }

        public void setParent(HelpItem parent) {
            this.parent = parent;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof HelpItem && getName().equals(((HelpItem) obj).getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        public String getStyleAttribs() {
            return styleAttribs;
        }

        public void setStyleAttribs(String styleAttribs) {
            this.styleAttribs = styleAttribs;
        }
    }

/*
* THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE CALIFORNIA
* INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S. GOVERNMENT CONTRACT WITH
* THE NATIONAL AERONAUTICS AND SPACE ADMINISTRATION (NASA). THE SOFTWARE
* IS TECHNOLOGY AND SOFTWARE PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS
* AND IS PROVIDED AS-IS TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND,
* INCLUDING ANY WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR
* A PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC 2312-2313)
* OR FOR ANY PURPOSE WHATSOEVER, FOR THE SOFTWARE AND RELATED MATERIALS,
* HOWEVER USED.
*
* IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA BE LIABLE
* FOR ANY DAMAGES AND/OR COSTS, INCLUDING, BUT NOT LIMITED TO, INCIDENTAL
* OR CONSEQUENTIAL DAMAGES OF ANY KIND, INCLUDING ECONOMIC DAMAGE OR INJURY TO
* PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE
* ADVISED, HAVE REASON TO KNOW, OR, IN FACT, SHALL KNOW OF THE POSSIBILITY.
*
* RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF THE SOFTWARE
* AND ANY RELATED MATERIALS, AND AGREES TO INDEMNIFY CALTECH AND NASA FOR
* ALL THIRD-PARTY CLAIMS RESULTING FROM THE ACTIONS OF RECIPIENT IN THE USE
* OF THE SOFTWARE.
*/

}
/*
* THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE CALIFORNIA
* INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S. GOVERNMENT CONTRACT WITH
* THE NATIONAL AERONAUTICS AND SPACE ADMINISTRATION (NASA). THE SOFTWARE
* IS TECHNOLOGY AND SOFTWARE PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS
* AND IS PROVIDED AS-IS TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND,
* INCLUDING ANY WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR
* A PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC 2312-2313)
* OR FOR ANY PURPOSE WHATSOEVER, FOR THE SOFTWARE AND RELATED MATERIALS,
* HOWEVER USED.
*
* IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA BE LIABLE
* FOR ANY DAMAGES AND/OR COSTS, INCLUDING, BUT NOT LIMITED TO, INCIDENTAL
* OR CONSEQUENTIAL DAMAGES OF ANY KIND, INCLUDING ECONOMIC DAMAGE OR INJURY TO
* PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE
* ADVISED, HAVE REASON TO KNOW, OR, IN FACT, SHALL KNOW OF THE POSSIBILITY.
*
* RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF THE SOFTWARE
* AND ANY RELATED MATERIALS, AND AGREES TO INDEMNIFY CALTECH AND NASA FOR
* ALL THIRD-PARTY CLAIMS RESULTING FROM THE ACTIONS OF RECIPIENT IN THE USE
* OF THE SOFTWARE.
*/
