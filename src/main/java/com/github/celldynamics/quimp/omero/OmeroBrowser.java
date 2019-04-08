package com.github.celldynamics.quimp.omero;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;
import org.scijava.io.IOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimpException;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import edu.emory.mathcs.backport.java.util.Collections;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import net.imagej.Dataset;
import net.imagej.omero.DefaultOMEROSession;
import net.imagej.omero.OMEROLocation;
import net.imagej.omero.OMEROService;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.cli.ErrorHandler;
import ome.formats.importer.cli.LoggingImportMonitor;
import omero.ServerError;
import omero.client;
import omero.api.IMetadataPrx;
import omero.api.RawFileStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;
import omero.model.Annotation;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.sys.ParametersI;

/**
 * Provide access to Omero.
 * 
 * @author p.baniukiewicz
 *
 */
public class OmeroBrowser implements Closeable {
  private static final String NAMESPACE = "QCONF";
  static final Logger LOGGER = LoggerFactory.getLogger(OmeroBrowser.class.getName());
  static final int DEF_PORT = 4064;

  private Gateway gateway = null;
  private SecurityContext ctx;
  private String user;
  private String host;
  private String pass;
  private client client;
  private DefaultOMEROSession ds;
  OMEROService os;
  Context context;
  private int port;

  /**
   * Initialise browser.
   * 
   * <p>Do not open connection. Use {@link #connect()} instead.
   * 
   * @param user omero user name
   * @param pass omero password
   * @param host host
   * @param port port, see {@link #DEF_PORT}
   */
  public OmeroBrowser(String user, String pass, String host, int port) {
    this.port = port;
    this.host = host;
    this.user = user;
    this.pass = pass;
  }

  /**
   * Connect to Omero.
   * 
   * <p>All fields provided to {@link #OmeroBrowser(String, String, String, int)} must contain
   * nonempty strings. Throw exception if connection unsuccessful.
   * 
   * @throws DSOutOfServiceException connection error
   * @throws QuimpException if any of fields: user/pass/host is empty or null
   * @throws URISyntaxException on Omero error
   * @throws CannotCreateSessionException on Omero error
   * @throws PermissionDeniedException on Omero error
   * @throws ServerError on Omero error
   */
  public void connect() throws DSOutOfServiceException, QuimpException, URISyntaxException,
          ServerError, PermissionDeniedException, CannotCreateSessionException {
    silentClose(); // close old connection
    LOGGER.debug("Opening connection:" + user + ", " + pass + ", " + host);
    if (user == null || user.isEmpty() || host == null || host.isEmpty() || pass == null
            || pass.isEmpty()) {
      throw new QuimpException("One of required fields is empty");
    }
    OMEROLocation credentials = new OMEROLocation(host, port, user, pass);
    context = new Context();
    os = context.getService(OMEROService.class);
    ds = new DefaultOMEROSession(credentials, os);
    client = ds.getClient();
    gateway = ds.getGateway();
    ctx = ds.getSecurityContext();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    silentClose();
  }

  /**
   * Close Omero without exception.
   */
  public void silentClose() {
    if (gateway != null) {
      ds.close();
      client.closeSession();
      gateway.disconnect();
      os.dispose();
      context.dispose();
      LOGGER.debug("Omero disconnected");
    }
  }

  /**
   * List projects.
   * 
   * @return list of projects for user.
   * @throws DSOutOfServiceException on Omero error
   * @throws DSAccessException on Omero error
   * @throws ExecutionException on Omero error
   */
  public List<ProjectData> listProjects()
          throws DSOutOfServiceException, DSAccessException, ExecutionException {
    BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
    return new ArrayList<ProjectData>(browse.getProjects(ctx));
  }

  /**
   * List datasets.
   * 
   * @return List of datasets for user.
   * @throws DSOutOfServiceException on Omero error
   * @throws DSAccessException on Omero error
   * @throws ExecutionException on Omero error
   */
  public List<DatasetData> listDatasets()
          throws DSOutOfServiceException, DSAccessException, ExecutionException {
    BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
    return new ArrayList<DatasetData>(browse.getDatasets(ctx));
  }

  /**
   * Open dataset.
   * 
   * @param name dataset name
   * @return images from dataset
   * @throws ExecutionException on Omero error
   * @throws DSOutOfServiceException on Omero error
   * @throws DSAccessException on Omero error
   */
  public Collection<ImageData> openDataset(DatasetData name)
          throws ExecutionException, DSOutOfServiceException, DSAccessException {
    BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
    List<Long> ida = new ArrayList<>();
    ida.add(name.getId());
    return browse.getImagesForDatasets(ctx, ida);

  }

  /**
   * Find dataset of specified name.
   * 
   * @param name name of dataset
   * @return dataset object
   * @throws DSOutOfServiceException on Omero error
   * @throws DSAccessException on Omero error
   * @throws ExecutionException on Omero error
   */
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
   * Find dataset of specified id.
   * 
   * @param id id of dataset
   * @return dataset object
   * @throws DSOutOfServiceException on Omero error
   * @throws DSAccessException on Omero error
   * @throws ExecutionException on Omero error
   */
  public DatasetData findDataset(Long id)
          throws DSOutOfServiceException, DSAccessException, ExecutionException {
    for (DatasetData ds : listDatasets()) {
      if (ds.getId() == id) {
        LOGGER.debug("Found dataset of id " + id + " (" + ds.toString() + ")");
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

  /**
   * Find image of specified id in dataset.
   * 
   * @param imageId id to find
   * @param name dataset name
   * @return image object
   * @throws ExecutionException on Omero error
   * @throws DSOutOfServiceException on Omero error
   * @throws DSAccessException on Omero error
   */
  public ImageData findImage(Long imageId, DatasetData name)
          throws ExecutionException, DSOutOfServiceException, DSAccessException {
    for (ImageData im : openDataset(name)) {
      if (im.getId() == imageId) {
        LOGGER.debug("Found image of id " + imageId + " (" + imageId.toString() + ")");
        return im;
      }
    }
    return null;

  }

  /**
   * @param pathsToImages
   * @param name
   * @throws Exception
   */
  public void upload(String[] pathsToImages, DatasetData name) throws Exception {
    LOGGER.info("Trying to upload: " + Arrays.toString(pathsToImages));
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
    LOGGER.info("Image uploaded");

  }

  /**
   * Upload attachment and attach it to image from dataset.
   * 
   * <p>Image is already in dataset.
   * 
   * @param imageName image name in dataset
   * @param pathToAttach path to attachment
   * @param name dataset name
   * @throws IOException on file error
   * @throws DSOutOfServiceException on Omero error
   * @throws DSAccessException on Omero error
   * @throws ServerError on Omero error
   * @throws ExecutionException on Omero error
   * @throws QuimpException on Omero error - no image in dataset
   */
  public void upload(String imageName, String pathToAttach, DatasetData name)
          throws IOException, DSOutOfServiceException, DSAccessException, ServerError,
          ExecutionException, QuimpException {
    LOGGER.info("Trying to upload attachement: " + pathToAttach + " to " + imageName);

    File file = new File(pathToAttach);
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
    DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
    originalFile = (OriginalFile) dm.saveAndReturnObject(ctx, originalFile);

    // Initialize the service to load the raw data
    RawFileStorePrx rawFileStore = gateway.getRawFileService(ctx);

    long inc = file.length();
    long pos = 0;
    int rlen;
    byte[] buf = new byte[(int) inc];
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
    // The description set above e.g. PointsModel
    fa.setDescription(omero.rtypes.rstring("PointsModel"));
    // The name space you have set to identify the file annotation.
    fa.setNs(omero.rtypes.rstring(NAMESPACE));

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
    LOGGER.info("Attachement uploaded");

  }

  /**
   * Download image and attachment.
   * 
   * @param image image with attachment (if no attachment only image is downloaded)
   * @param path path to file (should be not file name)
   * @throws DSOutOfServiceException on Omero error
   * @throws ServerError on Omero error
   * @throws DSAccessException on Omero error
   * @throws ExecutionException on Omero error
   * @throws IOException on file error
   * @throws URISyntaxException on Omero error
   * @throws PermissionDeniedException on Omero error
   * @throws CannotCreateSessionException on Omero error
   */
  public void download(ImageData image, Path path)
          throws DSOutOfServiceException, ServerError, DSAccessException, ExecutionException,
          IOException, URISyntaxException, PermissionDeniedException, CannotCreateSessionException {

    if (path.toFile().isFile()) {
      throw new IllegalArgumentException("Path must point to folder");
    }

    Dataset img = os.downloadImage(client, image.getId());
    IOService io = context.getService(IOService.class);
    Path imageFile = path.resolve(image.getName());
    io.save(img, imageFile.toString());
    long[] dim = new long[img.numDimensions()];
    img.dimensions(dim);
    LOGGER.info("Saved: " + imageFile.toString() + " : " + img.toString() + " dim: "
            + Arrays.toString(dim));

    long userId = gateway.getLoggedInUser().getId();
    List<String> nsToInclude = new ArrayList<String>();
    nsToInclude.add(NAMESPACE);
    List<String> nsToExclude = new ArrayList<String>();
    ParametersI param = new ParametersI();
    param.exp(omero.rtypes.rlong(userId)); // load the annotation for a given user.
    IMetadataPrx proxy = gateway.getMetadataService(ctx);
    List<Long> ids = new ArrayList<>();
    ids.add(image.getId());
    Map<Long, List<Annotation>> annotations =
            proxy.loadSpecifiedAnnotationsLinkedTo(FileAnnotation.class.getName(), nsToInclude,
                    nsToExclude, Image.class.getName(), ids, param);
    LOGGER.debug("Length: " + annotations.size());
    // for (Annotation a : annotations.get(image.getId())) {
    // if (a instanceof FileAnnotation) {
    // FileAnnotationData fa = new FileAnnotationData((FileAnnotation) a);
    // LOGGER.debug("id " + fa.getFileID());
    // }
    // }

    if (annotations.get(image.getId()) != null) {
      Iterator<Annotation> j = annotations.get(image.getId()).iterator();
      Annotation annotation;
      FileAnnotationData fa;
      RawFileStorePrx store = gateway.getRawFileService(ctx);
      File qconfFile =
              path.resolve(FilenameUtils.removeExtension(image.getName()) + ".QCONF").toFile();
      int index = 0;

      int inc;
      try (FileOutputStream stream = new FileOutputStream(qconfFile)) {
        while (j.hasNext()) {
          annotation = j.next();
          if (annotation instanceof FileAnnotation && index == 0) {
            fa = new FileAnnotationData((FileAnnotation) annotation);
            // // The id of the original file
            // create the original file object.
            OriginalFile originalFile = new OriginalFileI();
            originalFile.setName(omero.rtypes.rstring(qconfFile.getName()));
            originalFile.setPath(omero.rtypes.rstring(qconfFile.getParent()));
            originalFile.setSize(omero.rtypes.rlong(fa.getFileSize()));
            final ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithmI();
            checksumAlgorithm.setValue(omero.rtypes.rstring(ChecksumAlgorithmSHA1160.value));
            originalFile.setHasher(checksumAlgorithm);
            originalFile.setMimetype(omero.rtypes.rstring("application/octet-stream"));
            // Now we save the originalFile object
            DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
            originalFile = (OriginalFile) dm.saveAndReturnObject(ctx, originalFile);

            store.setFileId(fa.getFileID());
            int offset = 0;
            inc = (int) fa.getFileSize();
            long size = originalFile.getSize().getValue();
            try {
              for (offset = 0; (offset + inc) < size;) {
                stream.write(store.read(offset, inc));
                offset += inc;
              }
            } finally {
              stream.write(store.read(offset, (int) (size - offset)));
              LOGGER.info("Saved: " + qconfFile.toString() + " from image " + image.getId());
            }
            index++;
          }
        }
      } finally {
        store.close();
      }
    } else {
      LOGGER.warn("There is no QCONF file attached to image [ " + image.getId() + "]");
    }
  }

  /**
   * Dummy tests.
   * 
   * @param args args
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

    try (OmeroBrowser client = new OmeroBrowser(user, pass, host, DEF_PORT)) {
      client.connect();
      LOGGER.debug("Projects:");
      client.listDatasets().stream().forEach(x -> LOGGER.debug("dataset " + x.getName()));
      // pick dataset
      DatasetData ds = client.findDataset("Q");
      // list images
      client.openDataset(ds).stream()
              .forEach(x -> LOGGER.debug("image: " + x.getName() + ": " + x.getId()));

      // client.upload(new String[] {
      // "src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.tif kept stack.tif" }, ds);
      // client.upload("fluoreszenz-test.tif kept stack.tif",
      // "src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.QCONF", ds);
      client.download(client.findImage(194021L, ds), Paths.get("./").toAbsolutePath());

      // client.downloadIJ_TEST(); // this or these above

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // https://github.com/ome/minimal-omero-client/blob/master/src/main/java/com/example/SimpleConnection.java
    // https://docs.openmicroscopy.org/omero/5.4.10/developers/Java.html

  }

}
