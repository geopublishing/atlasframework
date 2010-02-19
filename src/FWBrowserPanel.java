///*
// *  SDSS Framework - This file is part of the Spatial Decision Support System Framework Platform
// *
// *  FWBrowserPanel.java
// *
// *  Responsible developper: Andreas Enders, Geographical Institut, Hydrology Research Group, Meckenheimer Allee 166, 53115 Bonn, Germany
// *                                          IMPETUS Project, www.impetus.uni-koeln.de
// *  Contact Information:                    info@andreas-enders.de
// */
//
//package de.isdss.util.framework.ui.panel;
//
//import de.isdss.util.framework.functionality.FWAbstractFunctionality;
//import de.isdss.util.service.IOHelper;
//import de.isdss.util.service.StringHelper;
//
//import java.awt.BorderLayout;
//import java.awt.event.ActionEvent;
//
//import org.apache.log4j.Category;
//import org.apache.log4j.Logger;
//import org.jdesktop.jdic.browser.WebBrowser;
//import org.jdesktop.jdic.browser.WebBrowserEvent;
//import org.jdesktop.jdic.browser.WebBrowserListener;
//
///**
// * @author andi
// *
// */
//public class FWBrowserPanel extends FWDefaultPanel
//{
//  /** Instance for Category. */
//  private static Category cCat = Logger.getLogger(FWBrowserPanel.class.getName());
//
//  private static final long serialVersionUID = 1L;
//
//  private WebBrowser iWebBrowser = new WebBrowser();
//
//  //------------------------------------------------------------------------------------------------
//
//  /**
//   * @param aParameterMap
//   * @param aFunctionID
//   */
//  public FWBrowserPanel(FWAbstractFunctionality aFunctionality)
//  {
//    this(aFunctionality, true);
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  /**
//   * @param aParameterMap
//   * @param aFunctionID
//   */
//  public FWBrowserPanel(FWAbstractFunctionality aFunctionality, boolean aCreateButtonPanel)
//  {
//    super(aFunctionality, aCreateButtonPanel);
//
//    initializeBrowser();
//    add(iWebBrowser, BorderLayout.CENTER);
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  /**
//   * @param aParameterMap
//   */
//  private void initializeBrowser()
//  {
//    //Use below code to check the status of the navigation process,
//    //or register a listener for the notification events.
//    iWebBrowser.addWebBrowserListener(
//        
//    //------------------------------------------------------------------------------------------------
//    new WebBrowserListener()
//    {
//      //------------------------------------------------------------------------------------------------
//      public void downloadStarted(WebBrowserEvent aEvent)
//      {
//        cCat.debug("Loading started."+iWebBrowser.getURL());
//      }
//
//      //------------------------------------------------------------------------------------------------
//      public void downloadCompleted(WebBrowserEvent aEvent)
//      {
//        setBackButtonEnabled(iWebBrowser.isBackEnabled());
//        setGoButtonEnabled(iWebBrowser.isForwardEnabled());
//
//      }
//
//      //------------------------------------------------------------------------------------------------
//      public void downloadProgress(WebBrowserEvent aEvent)
//      {
//        // updateStatusInfo("Loading in progress...");
//      }
//
//      //------------------------------------------------------------------------------------------------
//      public void downloadError(WebBrowserEvent aEvent)
//      {
//        cCat.debug("Loading error.");
//      }
//
//      //------------------------------------------------------------------------------------------------
//      public void documentCompleted(WebBrowserEvent aEvent)
//      {
//        cCat.debug("Document loading completed."+aEvent.toString());
//      }
//
//      //------------------------------------------------------------------------------------------------
//      public void titleChange(WebBrowserEvent aEvent)
//      {
//        cCat.debug("Title of the browser window changed.");
//      }
//
//      //------------------------------------------------------------------------------------------------
//      public void statusTextChange(WebBrowserEvent aEvent)
//      {
//        // updateStatusInfo("Status text changed.");
//      }
//    }
//
//    //------------------------------------------------------------------------------------------------    
//    
//    );
//    
//    
//    if (!StringHelper.isEmpty(iFunctionality.getStringParameterValue("contentfield"))
//        && !StringHelper.isEmpty(iFunctionality.getStringParameterValue(
//            iFunctionality.getStringParameterValue("contentfield"))))
//    {
//      setContent(iFunctionality.getStringParameterValue(
//          iFunctionality.getStringParameterValue("contentfield")).toString());
//    }
//    else
//    {
//      loadURL(iFunctionality.getStringParameterValue("url").toString());
//    }
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  @Override
//  protected void back()
//  {
//    iWebBrowser.back();
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  @Override
//  protected void cancel()
//  {
//    super.cancel();
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  @Override
//  protected void go()
//  {
//    iWebBrowser.forward();
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  @Override
//  protected void print()
//  {
//    super.print();
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  /**
//   * @param aString
//   */
//  private void setContent(String aContentString)
//  {
//    iWebBrowser.setContent(aContentString);
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  /**
//   * Check the current input URL string in the address text field, load it,
//   * and update the status info and toolbar info.
//   */
//  void loadURL(String aURL)
//  {
//    iWebBrowser.setURL(IOHelper.checkURL(aURL));
//  }
//
//  //------------------------------------------------------------------------------------------------
//
//  class Browser_jBackButton_actionAdapter implements java.awt.event.ActionListener 
//  {
//    FWBrowserPanel iAdaptee;
//
//    Browser_jBackButton_actionAdapter(FWBrowserPanel aAdaptee) 
//    {
//      iAdaptee = aAdaptee;
//    }
//
//    public void actionPerformed(ActionEvent e) 
//    {
//      iAdaptee.back();
//    }
//  }
//
//
//  //------------------------------------------------------------------------------------------------
//
//  class Browser_jForwardButton_actionAdapter implements java.awt.event.ActionListener 
//  {
//    FWBrowserPanel iAdaptee;
//
//    Browser_jForwardButton_actionAdapter(FWBrowserPanel aAdaptee) 
//    {
//      iAdaptee = aAdaptee;
//    }
//
//    public void actionPerformed(ActionEvent e) 
//    {
//      iAdaptee.go();
//    }
//}
//
//
//}

