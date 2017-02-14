/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang;

import org.ballerinalang.util.program.BLangPackages;
import org.ballerinalang.util.repository.PackageRepository;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.8.0
 */
public class BLangProgramArchiveBuilder {

    private static final String BAL_INF_DIR_NAME = "BAL_INF";
    private static final String BALLERINA_CONF = "ballerina.conf";
    private static final String balVersionText = "ballerina-version: 0.8.0";
    private static final String mainPackageLinePrefix = "main-function: ";

    public void build(BLangProgram bLangProgram) {
        String outFileName;
        String entryPoint = bLangProgram.getEntryPoint();
        if (entryPoint.endsWith(".bal")) {
            outFileName = entryPoint.substring(0, entryPoint.length() - 4) + ".bpz";
        } else {
            Path pkgPath = Paths.get(entryPoint);
            outFileName = pkgPath.getName(pkgPath.getNameCount() - 1).toString() + ".bpz";
        }

        createArchive(bLangProgram, outFileName);
    }

    public void build(BLangProgram bLangProgram, String outFileName) {
        if (outFileName == null || outFileName.isEmpty()) {
            throw new IllegalStateException("output name cannot be empty");
        }

        if (!outFileName.endsWith(".bpz")) {
            outFileName += ".bpz";
        }

        createArchive(bLangProgram, outFileName);
    }

    private void createArchive(BLangProgram bLangProgram, String outFileName) {
        Map<String, String> zipFSEnv = new HashMap<>();
        zipFSEnv.put("create", "true");

        URI zipFileURI = URI.create("jar:file:" + Paths.get(outFileName).toUri().getPath());
        try (FileSystem zipFS = FileSystems.newFileSystem(zipFileURI, zipFSEnv)) {
            addProgramToArchive(bLangProgram, zipFS);
            addBallerinaConfFile(zipFS, bLangProgram.getEntryPoint());
        } catch (IOException e) {
            // TODO Handler error
            e.printStackTrace();
        }
    }


    private void addProgramToArchive(BLangProgram bLangProgram,
                                     FileSystem zipFS) throws IOException {
        BLangPackage mainPkg = bLangProgram.getMainPackage();
        if (mainPkg.getPackagePath().equals(".")) {
            String fileName = bLangProgram.getEntryPoint();
            PackageRepository.PackageSource packageSource = mainPkg.getPackageRepository().loadFile(Paths.get(fileName));
            addPackageSourceToArchive(packageSource, Paths.get("."), zipFS);
        }

        addPackagesToArchive(bLangProgram.getPackages(), zipFS);
    }

    private void addPackagesToArchive(BLangPackage[] bLangPackages,
                                      FileSystem zipFS) throws IOException {

        for (BLangPackage bLangPackage : bLangPackages) {
            if (bLangPackage.getPackagePath().equals(".")) {
                continue;
            }

            Path packagePath = BLangPackages.getPathFromPackagePath(bLangPackage.getPackagePath());
            PackageRepository.PackageSource packageSource = bLangPackage.getPackageRepository().loadPackage(packagePath);
            addPackageSourceToArchive(packageSource, packagePath, zipFS);
        }
    }

    private void addPackageSourceToArchive(PackageRepository.PackageSource packageSource, Path packagePath,
                                           FileSystem zipFS) throws IOException {
        for (Map.Entry<String, InputStream> mapEntry : packageSource.getSourceFileStreamMap().entrySet()) {
            Path root = zipFS.getPath("/");
            Path dest = zipFS.getPath(root.toString(),
                    packagePath.resolve(mapEntry.getKey()).toString());

            copyFileToZip(mapEntry.getValue(), dest);
        }
    }

    private void copyFileToZip(InputStream srcInputStream, Path destPath) throws IOException {
        Path parent = destPath.getParent();
        if (Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        Files.copy(srcInputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void addBallerinaConfFile(FileSystem zipFS, String entryPoint) throws IOException {
        final Path root = zipFS.getPath("/");
        final Path dest = zipFS.getPath(root.toString(), BAL_INF_DIR_NAME, BALLERINA_CONF);

        String balConfContent = balVersionText + "\n" + mainPackageLinePrefix + zipFS.getPath(entryPoint).toString() + "\n";
        InputStream stream = new ByteArrayInputStream(balConfContent.getBytes(StandardCharsets.UTF_8));
        copyFileToZip(stream, dest);
    }
}
