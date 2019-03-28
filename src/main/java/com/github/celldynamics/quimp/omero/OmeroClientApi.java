package com.github.celldynamics.quimp.omero;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimpException;

import edu.emory.mathcs.backport.java.util.Collections;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.cli.ErrorHandler;
import ome.formats.importer.cli.LoggingImportMonitor;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.enums.ChecksumAlgorithmSHA1160;

/**
 * @author p.baniukiewicz
 *
 */
public class OmeroClientApi implements Closeable {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  static final Logger LOGGER = LoggerFactory.getLogger(OmeroClientApi.class.getName());
  public static final int DEF_PORT = 4064;

  private Gateway gateway = null;
  private SecurityContext ctx;
  private String user;
  private String host;
  private String pass;
  private int port;

  public OmeroClientApi(String user, String pass, String host, int port)
          throws DSOutOfServiceException {
    LOGGER.debug("Opening connection:" + user + ", " + pass + ", " + host);
    this.port = port;
    this.host = host;
    this.user = user;
    this.pass = pass;
    LoginCredentials cred = new LoginCredentials(user, pass, host, port);
    gateway = new Gateway(new LoggerWrapper());
    ExperimenterData experimenter = gateway.connect(cred);
    LOGGER.debug("User: " + experimenter.getLastName() + ", " + experimenter.getEmail());
    ctx = new SecurityContext(experimenter.getGroupId());
  }

  @Override
  public void close() throws IOException {
    if (gateway != null) {
      gateway.disconnect();
      LOGGER.debug("Omero disconnected");
    }

  }

  public List<ProjectData> listProjects()
          throws DSOutOfServiceException, DSAccessException, ExecutionException {
    BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
    return new ArrayList<ProjectData>(browse.getProjects(ctx));
  }

  public List<DatasetData> listDatasets()
          throws DSOutOfServiceException, DSAccessException, ExecutionException {
    BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
    return new ArrayList<DatasetData>(browse.getDatasets(ctx));
  }

  public Collection<ImageData> openDataset(DatasetData name)
          throws ExecutionException, DSOutOfServiceException, DSAccessException {
    BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
    List<Long> ida = new ArrayList<>();
    ida.add(name.getId());
    return browse.getImagesForDatasets(ctx, ida);

  }

  public DatasetData findDataset(String name)
          throws DSOutOfServiceException, DSAccessException, ExecutionException {
    for (DatasetData ds : listDatasets()) {
      if (ds.getName().equals(name)) {
        LOGGER.debug("Found dataset of name " + name + " (" + ds.toString() + ")");
        return ds;
      }
    }
    return null;
  }

  /**
   * Find image of specified name in the dataset.
   * 
   * <p>If there is more than 1 image with specified name, then depending on
   * <tt>allowDuplicates</tt> the newest will be returned (true) or exception thrown (false).
   * 
   * @param imageName image name to look for
   * @param name dataset name
   * @param allowDuplicates if true newest image is returned if there is more than 1 image with
   *        imageName in the dataset, otherwise exception is thrown
   * @return found image object
   * 
   * @throws ExecutionException omero error
   * @throws DSOutOfServiceException omero error
   * @throws DSAccessException omero error
   * @throws QuimpException if there are multiple images with the same name and
   *         allowDuplicates==false
   */
  public ImageData findImage(String imageName, DatasetData name, boolean allowDuplicates)
          throws ExecutionException, DSOutOfServiceException, DSAccessException, QuimpException {
    ArrayList<ImageData> imgs = new ArrayList<>();
    for (ImageData im : openDataset(name)) {
      if (im.getName().equals(imageName)) {
        LOGGER.debug("Found image of name " + name + " (" + im.toString() + "), inserted: "
                + im.getInserted());
        imgs.add(im);
      }
    }
    if (imgs.size() > 1 && allowDuplicates == false) {
      throw new QuimpException(
              "There are at least two images with name " + imageName + " in dataset");
    }
    if (imgs.size() == 0) {
      return null;
    }
    if (imgs.size() == 1) {
      return imgs.get(0);
    } else { // return newest
      Long now = System.currentTimeMillis(); // current time
      ArrayList<Long> deltas = new ArrayList<>(); // time from now for each file (with same name)
      for (ImageData im : imgs) { // check each file
        Long timeFile = im.getInserted().getTime(); // get upload time
        deltas.add(now - timeFile); // store difference from now
      }
      int closestInd = deltas.indexOf(Collections.min(deltas)); // index of smallest diff
      return imgs.get(closestInd); // file under this index
    }

  }

  public void upload(String[] pathsToImages, DatasetData name) {
    LOGGER.debug("Trying to upload: " + Arrays.toString(pathsToImages));
    ImportConfig config = new ome.formats.importer.ImportConfig();
    config.email.set("");
    config.sendFiles.set(true);
    config.sendReport.set(false);
    config.contOnError.set(false);
    config.debug.set(true);

    config.hostname.set(host);
    config.port.set(port);
    config.username.set(user);
    config.password.set(pass);
    config.target.set("omero.model.Dataset:" + name.getId());

    OMEROMetadataStoreClient store;
    try {
      store = config.createStore();
      store.logVersionInfo(config.getIniVersionNumber());
      OMEROWrapper reader = new OMEROWrapper(config);
      ImportLibrary library = new ImportLibrary(store, reader);

      ErrorHandler handler = new ErrorHandler(config);
      library.addObserver(new LoggingImportMonitor());

      ImportCandidates candidates = new ImportCandidates(reader, pathsToImages, handler);
      reader.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.ALL));
      library.importCandidates(config, candidates);

      store.logout();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void upload(String imageName, String pathToAttach, DatasetData name)
          throws IOException, DSOutOfServiceException, DSAccessException, ServerError,
          ExecutionException, QuimpException {
    LOGGER.debug("Trying to upload: " + imageName + " and " + pathToAttach);

    DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);

    File file = new File(pathToAttach);
    long INC = file.length();
    String name1 = Paths.get(pathToAttach).getFileName().toString();
    String absolutePath = file.getAbsolutePath();
    String path = absolutePath.substring(0, absolutePath.length() - name1.length());

    // create the original file object.
    OriginalFile originalFile = new OriginalFileI();
    originalFile.setName(omero.rtypes.rstring(name1));
    originalFile.setPath(omero.rtypes.rstring(path));
    originalFile.setSize(omero.rtypes.rlong(file.length()));
    final ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithmI();
    checksumAlgorithm.setValue(omero.rtypes.rstring(ChecksumAlgorithmSHA1160.value));
    originalFile.setHasher(checksumAlgorithm);
    originalFile.setMimetype(omero.rtypes.rstring("application/octet-stream"));
    // Now we save the originalFile object
    originalFile = (OriginalFile) dm.saveAndReturnObject(ctx, originalFile);

    // Initialize the service to load the raw data
    RawFileStorePrx rawFileStore = gateway.getRawFileService(ctx);

    long pos = 0;
    int rlen;
    byte[] buf = new byte[(int) INC];
    ByteBuffer bbuf;
    // Open file and read stream
    try (FileInputStream stream = new FileInputStream(file)) {
      rawFileStore.setFileId(originalFile.getId().getValue());
      while ((rlen = stream.read(buf)) > 0) {
        rawFileStore.write(buf, pos, rlen);
        pos += rlen;
        bbuf = ByteBuffer.wrap(buf);
        bbuf.limit(rlen);
      }
      originalFile = rawFileStore.save();
    } finally {
      rawFileStore.close();
    }
    // now we have an original File in DB and raw data uploaded.
    // We now need to link the Original file to the image using
    // the File annotation object. That's the way to do it.
    FileAnnotation fa = new FileAnnotationI();
    fa.setFile(originalFile);
    fa.setDescription(omero.rtypes.rstring("PointsModel")); // The description set above e.g.
    // PointsModel
    fa.setNs(omero.rtypes.rstring("QCONF")); // The name space you have set to identify the file
                                             // annotation.

    // save the file annotation.
    fa = (FileAnnotation) dm.saveAndReturnObject(ctx, fa);

    // now link the image and the annotation
    ImageAnnotationLink link = new ImageAnnotationLinkI();
    link.setChild(fa);
    ImageData imageData = findImage(imageName, name, true);
    link.setParent(imageData.asImage());
    // save the link back to the server.
    link = (ImageAnnotationLink) dm.saveAndReturnObject(ctx, link);
    // o attach to a Dataset use DatasetAnnotationLink;

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    String user;
    String pass;
    String host;
    Properties prop = new Properties();
    try (FileInputStream input = new FileInputStream(
            Paths.get(System.getProperty("user.home"), "omero.properties").toFile())) {
      prop.load(input);
      user = prop.getProperty("user");
      pass = prop.getProperty("pass");
      host = prop.getProperty("host");
    } catch (IOException ex) {
      ex.printStackTrace();
      return;
    }

    try (OmeroClientApi client = new OmeroClientApi(user, pass, host, DEF_PORT)) {
      LOGGER.debug("Projects:");
      client.listDatasets().stream().forEach(x -> LOGGER.debug("dataset " + x.getName()));
      // pick dataset
      DatasetData ds = client.findDataset("Q");
      // list images
      client.openDataset(ds).stream().forEach(x -> LOGGER.debug("image: " + x.getName()));

      client.upload(new String[] {
          "src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.tif kept stack.tif" }, ds);
      client.upload("fluoreszenz-test.tif kept stack.tif",
              "src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.QCONF", ds);
    } catch (IOException | DSOutOfServiceException | DSAccessException | ExecutionException
            | ServerError | QuimpException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // https://github.com/ome/minimal-omero-client/blob/master/src/main/java/com/example/SimpleConnection.java
    // https://docs.openmicroscopy.org/omero/5.4.10/developers/Java.html

  }

}
