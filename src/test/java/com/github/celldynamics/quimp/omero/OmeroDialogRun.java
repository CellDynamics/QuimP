package com.github.celldynamics.quimp.omero;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.swing.JDialog;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.ImageJ;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/**
 * Test UI.
 * 
 * @author p.baniukiewicz
 *
 */
public class OmeroDialogRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  static final Logger LOGGER = LoggerFactory.getLogger(OmeroDialogRun.class.getName());

  static DatasetData getMockedDataset(int len) {
    DatasetData ds = Mockito.mock(DatasetData.class);
    Mockito.when(ds.getId()).thenReturn((long) RandomUtils.nextInt(0, 65535));
    Mockito.when(ds.getName()).thenReturn(RandomStringUtils.random(len, true, false));
    return ds;
  }

  static List<DatasetData> getrandomDataset(int len, int num) {
    ArrayList<DatasetData> ret = new ArrayList<>();
    IntStream.range(0, num).forEach(i -> ret.add(getMockedDataset(len)));
    return ret;
  }

  static ImageData getMockedImage(int len) {
    ImageData ds = Mockito.mock(ImageData.class);
    Mockito.when(ds.getCreated()).thenReturn(new Timestamp(
            new Date(Math.abs(System.currentTimeMillis() - RandomUtils.nextLong())).getTime()));
    Mockito.when(ds.getName()).thenReturn(RandomStringUtils.random(len, true, false));
    Mockito.when(ds.getId()).thenReturn((long) RandomUtils.nextInt(0, 65535));
    return ds;
  }

  static List<ImageData> getrandomImages(int len, int num) {
    ArrayList<ImageData> ret = new ArrayList<>();
    IntStream.range(0, num).forEach(i -> ret.add(getMockedImage(len)));
    return ret;
  }

  /**
   * Test UI.
   * 
   * @param args
   */
  public static void main(String[] args) {
    new ImageJ();
    OmeroClient_ omc = Mockito.mock(OmeroClient_.class);
    Mockito.when(omc.connect()).thenReturn(true);
    List<DatasetData> listOfDatasets = getrandomDataset(7, 100);
    Mockito.when(omc.getDatasets()).thenReturn(listOfDatasets);
    List<ImageData> listOfImages1 = getrandomImages(7, 30);
    List<ImageData> listOfImages2 = getrandomImages(6, 10);
    List<ImageData> listOfImages3 = getrandomImages(5, 3);
    List<ImageData> listOfImages4 = getrandomImages(3, 0);
    Mockito.when(omc.getImages(Mockito.anyInt())).thenReturn(listOfImages1)
            .thenReturn(listOfImages2).thenReturn(listOfImages3).thenReturn(listOfImages4);
    Mockito.doAnswer((i) -> {
      TimeUnit.SECONDS.sleep(5);
      return null;
    }).when(omc).upload();
    OmeroBrowser omero = Mockito.mock(OmeroBrowser.class);
    omc.omero = omero;

    OmeroLoginDialog dialog = new OmeroLoginDialog(omc);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setVisible(true);

  }

}
