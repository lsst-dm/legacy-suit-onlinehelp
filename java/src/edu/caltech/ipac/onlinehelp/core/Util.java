package edu.caltech.ipac.onlinehelp.core;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Date: 6/25/12
 *
 * @author loi
 * @version $Id: Util.java,v 1.1 2012/08/07 17:10:14 loi Exp $
 */
public class Util {

    public static String getMetaInfo(String name) {
        if (isEmpty(name)) return null;

        NodeList<Element> meta = Document.get().getElementsByTagName("meta");
        for (int i = 0; i < meta.getLength(); i++) {
            final MetaElement m = MetaElement.as(meta.getItem(i));
            if (m != null && name.equals(m.getName())) {
                return m.getContent();
            }
        }
        return null;
    }

    public static Widget centerAlign(Widget w) {
        HorizontalPanel hp = new HorizontalPanel();
        hp.setWidth("100%");
        hp.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        hp.add(w);
        return hp;
    }

    public static Widget middleAlign(Widget w) {
        VerticalPanel p = new VerticalPanel();
        p.setHeight("100%");
        p.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        p.add(w);
        return p;
    }

    public static Widget rightAlign(Widget w) {
        SimplePanel wrapper = new SimplePanel();
        wrapper.setWidth("100%");
        wrapper.setWidget(w);
        DOM.setElementAttribute(wrapper.getElement(), "align", "right");
        return wrapper;
//        HorizontalPanel hp = new HorizontalPanel();
//        hp.setWidth("100%");
//        hp.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
//        hp.add(w);
//        return hp;
    }

    public static Widget makeLinkIcon(ImageResource iconUrl, String text,
                                      String tip,
                                      ClickHandler handler) {
        HorizontalPanel hp = new HorizontalPanel();
        Image image = new Image(iconUrl);
        image.setHeight("16px");
        hp.add(image);
        if (!isEmpty(text)) {
            Label label = new Label(text);
            if (tip != null) {
                label.setTitle(tip);
            }
            label.addClickHandler(handler);
            setStyles(label, "leftPadding", "3px");
            hp.add(label);
        }
        if (tip != null) {
            image.setTitle(tip);
        }
        image.addClickHandler(handler);
        return hp;
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }


    /**
     * return true if this component is visible and occupy space
     *
     * @param widget the widget to test
     * @return true if is on the display
     */
    public static boolean isOnDisplay(Widget widget) {
        if (widget != null) {
            boolean val = (widget.getOffsetHeight() * widget.getOffsetWidth() > 0)
                    && isVisible(widget.getElement());
            return val;
        }
        return false;
    }

    /**
     * return true if the given element is visible.  this is based on style attribtues. it is possible that a widget
     * is visible, but does not have width or height.
     *
     * @param elem the element to test
     * @return true if visible, false if not
     */
    public static boolean isVisible(com.google.gwt.dom.client.Element elem) {
        if (isHidden(elem)) {
            return false;
        } else {
            com.google.gwt.dom.client.Element p = elem.getParentElement();
            if (p != null) {
                return isVisible(p);
            } else {
                return true;
            }
        }
    }

    public static native boolean isHidden(com.google.gwt.dom.client.Element elem) /*-{
      return (elem.style.visibility == "hidden") || (elem.style.display == "none");
    }-*/;

    public static void setHidden(Widget w, boolean isHidden) {
        setHidden(w.getElement(), isHidden);
    }

    public static void setHidden(com.google.gwt.user.client.Element e, boolean isHidden) {
        String vs = isHidden ? "hidden" : "visible";
        DOM.setStyleAttribute(e, "visibility", vs);
    }

    public static Widget getTopParent(Widget w) {
        Widget retval = w;
        if (w != null) {
            while (retval.getParent() != null) {
                retval = retval.getParent();
            }
        }
        return retval;
    }

    public static void setStyle(Widget w, String style, String value) {
        DOM.setStyleAttribute(w.getElement(), style, value);
    }

    public static void setStyles(Widget w, String... s) {
        setStyles(w.getElement(), s);
    }

    public static void setStyles(com.google.gwt.user.client.Element e, String... s) {
        for (int i = 0; (i < s.length - 1); i += 2) {
            DOM.setStyleAttribute(e, s[i], s[i + 1]);
        }
    }
    
    public static String[] extractStyles(String s) {
        ArrayList<String> retval = new ArrayList<String>();
        String[] styles = s.split(";");
        for (String style : styles) {
            String[] parts = style.split(":", 2);
            if (parts.length > 1) {
                retval.add(parts[0].trim());
                retval.add(parts[1].trim());
            }
        }
        return retval.toArray(new String[retval.size()]);
    }

//====================================================================
//
//====================================================================

    public static native void open(String url, String name, String features) /*-{
      winref = $wnd.open(url, name, features);
      winref.focus()
    }-*/;


    public static class DockLayout extends DockLayoutPanel {
        public DockLayout() {
            super(Style.Unit.PX);
        }

        public static double getDockWidgetSize(Widget widget) {
            if (widget != null) {
                LayoutData lo = (LayoutData) widget.getLayoutData();
                if (lo != null) {
                    return lo.size;
                }
            }
            return 0;
        }

        public static void setWidgetChildSize(Widget widget, double size) {
            if (widget == null) return;
            LayoutData lo = (LayoutData) widget.getLayoutData();
            if (lo != null) {
                lo.size = size;
            }
        }

        public static void showWidget(DockLayoutPanel dockPanel, Widget widget) {
            show(widget);
            dockPanel.forceLayout();
        }

        public static void hideWidget(DockLayoutPanel dockPanel, Widget widget) {
            if (hide(widget)) {
                dockPanel.forceLayout();
            }
        }

        public static boolean isHidden(Widget widget) {
            if (widget != null) {
                LayoutData lo = (LayoutData) widget.getLayoutData();
                if (lo != null) {
                    return lo.hidden;
                }
            }
            return true;
        }

        protected static boolean hide(Widget widget) {
            if (widget != null) {
                LayoutData lo = (LayoutData) widget.getLayoutData();
                if (lo != null) {
                    if (!lo.hidden) {
                        lo.hidden = true;
                        lo.oldSize = lo.size;
                        lo.size = 0;

                        return true;
                    }
                }
            }
            return false;
        }

        protected static boolean show(Widget widget) {
            if (widget != null) {
                LayoutData lo = (LayoutData) widget.getLayoutData();
                if (lo != null) {
                    if (lo.hidden) {
                        double s = lo.oldSize > 0 ? lo.oldSize : lo.originalSize;
                        lo.size = s;
                        lo.hidden = false;

                        return true;
                    }
                }
            }
            return false;
        }

    }





    public static class SplitPanel extends DockLayout {

        public static void hideWidget(DockLayoutPanel splitPanel, Widget widget) {
            if (!isHidden(widget)) {
                hide(widget);
                setSplitterVisible(splitPanel, widget, false);
                splitPanel.forceLayout();
            }
        }

        public static void hideWidget(SplitLayoutPanel splitPanel, Widget widget) {
            hideWidget((DockLayoutPanel)splitPanel,widget);
        }


        public static void showWidget(DockLayoutPanel splitPanel, Widget widget) {
            if (isHidden(widget)) {
                show(widget);
                setSplitterVisible(splitPanel, widget, true);
                splitPanel.forceLayout();
            }
        }

        public static void showWidget(SplitLayoutPanel splitPanel, Widget widget) {
            showWidget((DockLayoutPanel)splitPanel,widget);
        }

        private static void setSplitterVisible(DockLayoutPanel splitPanel, Widget widget, boolean isVisible) {
            int idx = splitPanel.getWidgetIndex(widget);
            Widget splitter = splitPanel.getWidget(idx + 1);  // this should be the splitter
            if (splitter != null) {
                splitter.setVisible(isVisible);
            }
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
