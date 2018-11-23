package com.android.iflyings.player.utils

import android.content.Context
import android.os.storage.StorageManager

import java.io.File
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList

object FileUtils {

    fun getAllFileInFolder(folderPath: String, filters: List<String>): List<String> {
        val fileLists = ArrayList<String>()
        val file = File(folderPath)
        if (file.isDirectory && file.list() != null && file.list().isNotEmpty()) {
            for (f in file.listFiles()) {
                if (f.isFile) {
                    val name = f.name
                    for (filter in filters) {
                        if (name.toLowerCase().endsWith(filter)) {
                            fileLists.add(f.path)
                            break
                        }
                    }
                } else if (f.isDirectory) {
                    fileLists.addAll(getAllFileInFolder(f.path, filters))
                }
            }
        }
        return fileLists
    }

    fun getStorageList(context: Context): List<File> {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val storageDataList = ArrayList<File>()
        try {
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz.getMethod("getPath")
            //Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            val result = getVolumeList.invoke(storageManager)
            val length = Array.getLength(result)
            for (i in 0 until length) {
                val storageVolumeElement = Array.get(result, i)
                val path = getPath.invoke(storageVolumeElement) as String
                //boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                storageDataList.add(File(path))
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return storageDataList
    }

}
