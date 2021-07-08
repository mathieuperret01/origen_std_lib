package origen.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import xoc.dta.datatypes.MultiSite2DLongArray;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.datatypes.MultiSiteLongArray;
import xoc.dta.resultaccess.datatypes.BitSequence.BitOrder;

/**
 * Generic data holder class
 *
 * <p>Can be used for example to store data before patching. Data elements are of type long. The
 * implementation class is responsible for mapping this data to the right format
 *
 * <p>Use: It's basically a key-value pair per data point. This format is efficient for large arrays
 * with little set data. It's not so efficient for a small array with many datapoints set. The key
 * is the address and the value is called data. Everything is stored in a multisitelongarray for
 * efficient patching.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * OrigenData mem = new OrigenData();
 * mem.setData(10, 0x55);
 * mem.setData(12, 0x55);
 * println("The same? " + mem.allSitesTheSame(10));
 * mem.setDataOnSite(1, 10, 11);
 * println("The same? " + mem.allSitesTheSame(10));
 *
 * mem.setDataOnSite(4, 100, 44);
 * mem.setDataOnSite(4, 200, 44);
 * mem.setDataOnSite(4, 300, 44);
 * mem.setData(60, 60);
 * mem.printData();
 *
 * println("Addr 10 site 1 set? : " + mem.addrIsSet(1, 10));
 * int addr = 60;
 * if (mem.allSitesTheSame(addr)) {
 *     println(mem.getDataCommon(addr));
 * } else {
 *     println(mem.getDataMSL(addr));
 * }
 * }</pre>
 */
public class OrigenData {
  private MultiSiteLongArray mem_addr;
  private MultiSite2DLongArray mem_data;

  private ArrayList<Long> _sortedUniqueElements;
  // For printing purposes only
  public int bitPerDataElement = 34;

  private boolean _anythingSet = false;
  /** Constructor, initialized empty address and data arrays */
  public OrigenData() {
    mem_addr = new MultiSiteLongArray();
    mem_data = new MultiSite2DLongArray();
    _anythingSet = false;
  }

  public ArrayList<Long> getUniqueAddressList() {
    ArrayList<Long> list = new ArrayList<Long>();
    for (int site : mem_data.getActiveSites()) {
      long[] a = mem_addr.get(site);
      for (int i = 0; i < a.length; i++) {
        if (!list.contains(a[i])) {
          list.add(a[i]);
        }
      }
    }
    Collections.sort(list);
    Collections.reverse(list);
    return list;
  }

  public MultiSiteBoolean getActiveSitesOnAddr(long addr) {
    MultiSiteBoolean MSB = new MultiSiteBoolean();
    for (int site : mem_data.getActiveSites()) {
      MSB.set(site, addrIsSet(site, addr));
    }
    return MSB;
  }

  public int getUniqueAddr(int index) {
    _sortedUniqueElements = getUniqueAddressList();
    return _sortedUniqueElements.get(index).intValue();
  }

  public int getNumUniqueAddr() {
    _sortedUniqueElements = getUniqueAddressList();
    return _sortedUniqueElements.size();
  }

  public static <T extends Comparable<T>> List<Integer> sortIndex(final List<T> in) {
    ArrayList<Integer> index = new ArrayList<>();
    for (int i = 0; i < in.size(); i++) {
      index.add(i);
    }

    Collections.sort(
        index,
        new Comparator<Integer>() {
          @Override
          public int compare(Integer idx1, Integer idx2) {
            return in.get(idx1).compareTo(in.get(idx2));
          }
        });

    return index;
  }

  public void sort() {
    for (int site : mem_data.getActiveSites()) {
      long[] a = mem_addr.get(site);
      long[][] d = mem_data.get(site);

      List<Long> list = new ArrayList<Long>(a.length);
      for (long n : a) {
        list.add(n);
      }

      List<Integer> idx = sortIndex(list);

      long[] newA = a.clone();
      long[][] newD = d.clone();
      for (int i = 0; i < a.length; i++) {
        newA[i] = a[idx.get(i)];
        newD[i] = d[idx.get(i)];
      }
      mem_addr.set(site, newA);
      mem_data.set(site, newD);
    }
  }

  public boolean memEmpty() {
    return _anythingSet;
  }
  /**
   * Set data[] on 1 specific site for 1 address
   * Use this function when the size of data is > 64bits (max size of a long variable)
   * Split the data in an array of long with the same size
   * For ex: C28ESF3 has a data of 136bits, so data = [34bits, 34bits, 34bits, 34bits]
   *
   * @param site
   * @param addr
   * @param data
   */
  public void setDataOnSite(int site, long addr, long[] data) {
    long[] a = mem_addr.get(site);
    long[][] d = mem_data.get(site);
    if (a == null) {
      a = new long[0];
    }
    if (d == null) {
      d = new long[0][0];
    }
    int loc = valInAddr(a, addr);
    if (loc == d.length) {
      d = expand(d);
      a = expand(a);
    }
    d[loc] = data;
    a[loc] = addr;
    mem_data.set(site, d);
    mem_addr.set(site, a);
    _anythingSet = true;
  }
  
  /**
   * Set data on 1 specific site for 1 address
   * Use this function when the size of data is <= 64bits (max size of a long variable)
   * For ex: C402T has a data of 32bits, so data = 32bits
   * 
   * @param site
   * @param addr
   * @param data
   */
  public void setDataOnSitesmallData(int site, long addr, long data) {
    long[] a = mem_addr.get(site);
    long[][] d = mem_data.get(site);
    if (a == null) {
      a = new long[0];
    }
    if (d == null) {
      d = new long[0][0];
    }
    int loc = valInAddr(a, addr);
    if (loc == d.length) {
      d = expand(d);
      a = expand(a);
    }

    long[] data0 = new long[] {data};
    d[loc] = data0;
    a[loc] = addr;
    mem_data.set(site, d);
    mem_addr.set(site, a);
    _anythingSet = true;
  }

  /**
   * Set the data[] for a certain 32bit addr
   * Used for data size > 64bits (like C28ESF3, 136 bits)
   *
   * @param addr
   * @param data
   */
  public void setData(long addr, long[] data) {
    for (int site : mem_data.getActiveSites()) {
      setDataOnSite(site, addr, data);
    }
  }

  /**
   * Set the data for a certain 32bit addr
   * Used for data size <= 64bits (like C402T, 32 bits)
   *
   * @param addr
   * @param data
   */
  public void setDatasmallData(long addr, long data) {
    for (int site : mem_data.getActiveSites()) {
      setDataOnSitesmallData(site, addr, data);
    }
  }

  /**
   * Returns true if a certain address is set on ANY site
   *
   * @param addr
   * @return
   */
  public boolean addrIsSetAnySite(long addr) {
    for (int site : mem_data.getActiveSites()) {
      if (addrIsSet(site, addr)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if a certain address is set on a specific site
   *
   * @param site
   * @param addr
   * @return
   */
  public boolean addrIsSet(int site, long addr) {
    long[] a = mem_addr.get(site);
    int loc = valInAddr(a, addr);
    return loc != a.length;
  }

  /**
   * Returns the common data[] for all sites. Throws an error if this specific address is
   * Used for data size > 64bits (like C28ESF3, 136 bits)
   * site-specific
   *
   * @param addr
   * @return
   */
  public long[] getDataCommon(long addr) {
    int sites[] = mem_data.getActiveSites();
    if (allSitesTheSame(addr)) {
      return getDataPerSite(sites[0], addr);
    }
    throw new Error(
        "Not all sites have the same data, cannot give common data for this addr: " + addr);
  }

  /**
   * Returns the common data for all sites. Throws an error if this specific address is
   * Used for data size <= 64bits (like C402T, 32 bits)
   * site-specific
   *
   * @param addr
   * @return
   */
  public long getDataCommonsmallData(long addr) {
    int sites[] = mem_data.getActiveSites();
    if (allSitesTheSame(addr)) {
      return getDataPerSite(sites[0], addr)[0];
    }
    throw new Error(
        "Not all sites have the same data, cannot give common data for this addr: " + addr);
  }

    /**
   * Returns the common data[] for all sites.
   * Used for data size > 64bits (like C28ESF3, 136 bits)
   * @param site : any site active
   * @param addr : addr
   * @return
   */
  public long[] getDataCommon(int site, long addr) {
      return getDataPerSite(site, addr);

  }

   /**
   * Returns the common data for all sites.
   * Used for data size <= 64bits (like C402T, 32 bits)
   * @param site : any site active
   * @param addr : addr
   * @return
   */
  public long getDataCommonsmallData(int site, long addr) {
    return getDataPerSite(site, addr)[0];

}

  /**
   * Returns the site specific data[] for an address, returning -1 for the data if it has not been
   * previously set
   * Used for data size > 64bits (like C28ESF3, 136 bits)
   *
   * @param addr
   * @return
   */
  public MultiSiteLongArray getDataMSLA(long addr) {
    return getDataMSLA(addr, false, "");
  }

  /**
   * Returns the site specific data for an address, returning -1 for the data if it has not been
   * previously set
   * Used for data size <= 64bits (like C402T, 32 bits)
   * 
   * @param addr
   * @return
   */
  public MultiSiteLong getDataMSLsmallData(long addr) {
    return getDataMSLsmallData(addr, false, "");
  }


   /**
   * Returns the site specific data for an address, returning -1 for the data if it has not been
   * previously set
   * @param site
   * @param addr
   * @return
   */
  public long[] getDataMSLA(int site, long addr) {
    return getDataMSLA(site, addr, true, "");
  }

  /**
   * Returns the site specific data[] for an address, but raising and error with the given message if
   * the data has not been previously set
   * Used for data size > 64bits (like C28ESF3, 136 bits)
   *
   * @param addr
   * @return
   */
  public MultiSiteLongArray getDataMSLA(long addr, String errorMsg) {
    return getDataMSLA(addr, true, errorMsg);
  }

  /**
   * Returns the site specific data for an address, but raising and error with the given message if
   * the data has not been previously set
   * Used for data size <= 64bits (like C402T, 32 bits)
   *
   * @param addr
   * @return
   */
  public MultiSiteLong getDataMSLsmallData(long addr, String errorMsg) {
    return getDataMSLsmallData(addr, true, errorMsg);
  }

  /**
   * Returns the specific data[] for an address for all sites, but raising and error with the given message if
   * the data has not been previously set
   * Used for data size > 64bits (like C28ESF3, 136 bits)
   *
   * @param addr
   * @boolean error
   * @return
   * 
   */
  private MultiSiteLongArray getDataMSLA(long addr, boolean errorOnNotSet, String errorMsg) {
      MultiSiteLongArray result = new MultiSiteLongArray();
      for (int site : mem_data.getActiveSites()) {
        if (addrIsSet(site, addr)) {
          result.set(site, getDataPerSite(site, addr));
        } else {
          if (errorOnNotSet) {
            throw new Error(errorMsg);
          }
          result.set(site, new long[] {-1,-1,-1,-1}); // 4 parts (each parts corresponds to 34bits)
        }
      }
      return result;
    }

  /**
   * Returns the specific data for an address for all sites, but raising and error with the given message if
   * the data has not been previously set
   * Used for data size <= 64bits (like C402T, 32 bits)
   *
   * @param addr
   * @boolean error
   * @return
   * 
   */
  private MultiSiteLong getDataMSLsmallData(long addr, boolean errorOnNotSet, String errorMsg) {
      MultiSiteLongArray result = new MultiSiteLongArray();
      for (int site : mem_data.getActiveSites()) {
        if (addrIsSet(site, addr)) {
          result.set(site, getDataPerSite(site, addr));
        } else {
          if (errorOnNotSet) {
            throw new Error(errorMsg);
          }
          result.set(site, new long[] {-1,-1,-1,-1}); // 4 parts (each parts corresponds to 34bits)
        }
      }
      return result.getElement(0);
    }

  /**
   * Returns the specific data[] for an address for a specific site, but raising and error with the given message if
   * the data has not been previously set
   * Used for data size > 64bits (like C28ESF3, 136 bits)
   *
   * @param site
   * @param addr
   * @boolean error
   * @return
   * 
   */
  private long[] getDataMSLA(int site, long addr, boolean errorOnNotSet, String errorMsg) {
      long[] result = null;
        if (addrIsSet(site, addr)) {
          result = getDataPerSite(site, addr);
        } else {
          if (errorOnNotSet) {
            throw new Error("No Address set for site: " + site + "@addr: 0X" + Long.toHexString(addr));
          }
        }
      return result;
    }

  /**
   * Returns the specific data for an address for a specific site, but raising and error with the given message if
   * the data has not been previously set
   * Used for data size <= 64bits (like C402T, 32 bits)
   *
   * @param site
   * @param addr
   * @boolean error
   * @return
   * 
   */
  private long getDataMSLsmallData(int site, long addr, boolean errorOnNotSet, String errorMsg) {
      long[] result = null;
        if (addrIsSet(site, addr)) {
          result = getDataPerSite(site, addr);
        } else {
          if (errorOnNotSet) {
            throw new Error("No Address set for site: " + site + "@addr: 0X" + Long.toHexString(addr));
          }
        }
      return result[0];
    }

  /**
   * Returns whether or not all the sites have the same data for this addr
   *
   * @param addr
   * @return
   */
  public boolean allSitesTheSame(long addr) {
    long[] commonData = new long[]{-1,-1,-1,-1};
    boolean addrFound = false, addrNotFound = false;
    for (int site : mem_data.getActiveSites()) {
      long[][] d = mem_data.get(site);
      long[] a = mem_addr.get(site);
      int loc = valInAddr(a, addr);
      if (loc != a.length) {
        if (addrNotFound) {
          return false;
        }
        // Addr is found
        addrFound = true;

        for(int i = 0 ; i < 4 ; i++) {
            if (commonData[i] == -1) {
                commonData[i] = d[loc][i];
            }
            else {
                if(commonData[i] != d[loc][i]) {
                     // Not all data the same for this addr over all sits
                    return false;
                }
            }
        }
      } else {
        addrNotFound = true;
        // Addr is found on one site but not the other
        if (addrFound) {
          return false;
        }
      }
    }
    return true;
  }

  /** Print all set data for all sites */
  public void printData() {
    sort();
    System.out.println(getUniqueAddressList());
    for (int site : mem_data.getActiveSites()) {
      System.out.println("Site: " + site);
      long[][] d = mem_data.get(site);
      long[] a = mem_addr.get(site);
      for (int i = 0; i < d.length; i++) {
        System.out.println(
            a[i]
                + "\t"
                + OrigenHelpers.longToPaddedHexString(
                    d[i][0], bitPerDataElement / 4, BitOrder.RIGHT_TO_LEFT));
        System.out.println(
                a[i]
                    + "\t"
                    + OrigenHelpers.longToPaddedHexString(
                        d[i][1], bitPerDataElement / 4, BitOrder.RIGHT_TO_LEFT));
        System.out.println(
                a[i]
                    + "\t"
                    + OrigenHelpers.longToPaddedHexString(
                        d[i][2], bitPerDataElement / 4, BitOrder.RIGHT_TO_LEFT));
        System.out.println(
                a[i]
                    + "\t"
                    + OrigenHelpers.longToPaddedHexString(
                        d[i][3], bitPerDataElement / 4, BitOrder.RIGHT_TO_LEFT));
      }
    }
  }

  /** Clears all data on all sites */
  public void clearAllData() {
    // Lazy man's approach: Basically just throwing the reference to the old MSLarray away
    // Let's hope the garbage collector removes the old references nicely
    mem_addr = new MultiSiteLongArray();
    mem_data = new MultiSite2DLongArray();
    _anythingSet = false;
  }
  // Some private helper functions

  /**
   * Returns the location in the array for a certain address. Returns the last+1 location of the
   * array if addr is not found
   *
   * @param arr
   * @param val
   * @return
   */
  private int valInAddr(long[] arr, long val) {
    if (arr != null) {
      for (int i = 0; i < arr.length; i++) {
        if (val == arr[i]) {
          return i;
        }
      }
      return arr.length;
    }
    return 0;
  }

  /**
   * Get the data for a specific site. This is private because the testmethod should call
   * getDataMSL()
   *
   * @param site
   * @param addr
   * @return
   */
  private long[] getDataPerSite(int site, long addr) {
    long[][] d = mem_data.get(site);
    long[] a = mem_addr.get(site);
    int loc = valInAddr(a, addr);
    return d[loc];
  }

  /**
   * Expand the array by 1
   *
   * @param origArray
   * @return
   */
  private long[] expand(long[] origArray) {
    long[] newArray = new long[origArray.length + 1];
    System.arraycopy(origArray, 0, newArray, 0, origArray.length);
    return newArray;
  }

/**
 * Expand the array by 1
 *
 * @param origArray
 * @return
 */
private long[][] expand(long[][] origArray) {
  long[][] newArray = new long[origArray.length + 1][4];
  System.arraycopy(origArray, 0, newArray, 0, origArray.length);
  return newArray;
}
}

class DataPair<T> {
  private final T addr;
  private final T data;

  public DataPair(T first, T second) {
    addr = first;
    data = second;
  }

  public T addr() {
    return addr;
  }

  public T second() {
    return data;
  }
}
