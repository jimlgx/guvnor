/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.dtablexls.backend.server;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.base.options.CommentedOption;
import org.kie.guvnor.commons.data.events.AssetEditedEvent;
import org.kie.guvnor.commons.data.events.AssetOpenedEvent;
import org.kie.guvnor.commons.service.metadata.model.Metadata;
import org.kie.guvnor.commons.service.validation.model.BuilderResult;
import org.kie.guvnor.commons.service.verification.model.AnalysisReport;
import org.kie.guvnor.dtablexls.service.DecisionTableXLSService;
import org.kie.guvnor.services.metadata.MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.security.Identity;

@Service
@ApplicationScoped
public class DecisionTableXLSServiceImpl
        implements DecisionTableXLSService {
    private static final Logger log = LoggerFactory.getLogger( DecisionTableXLSServiceImpl.class );
    
    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private MetadataService metadataService;
    
    @Inject
    private Event<AssetEditedEvent> assetEditedEvent;
    
    @Inject
    private Event<AssetOpenedEvent> assetOpenedEvent;
    
    @Inject
    private Paths paths;

    @Inject
    private Identity identity;

    @Override
    public BuilderResult validate( final Path path,
                                   final String content ) {
        //TODO {porcelli} validate
        return new BuilderResult();
    }

    @Override
    public boolean isValid( final Path path,
                            final String content ) {
        return !validate( path, content ).hasLines();
    }

    @Override
    public AnalysisReport verify( Path path,
                                  String content ) {
        //TODO {porcelli} verify
        return new AnalysisReport();
    }

    @Override
    public InputStream load( Path path ) {
        assetOpenedEvent.fire( new AssetOpenedEvent( path ) );  
        return ioService.newInputStream(paths.convert( path ), null );
    }

    @Override
    public OutputStream save(final Path path) {
        log.info("USER:" + identity.getName() + " SAVING asset [" + path.getFileName() + "]");

        System.out.println("USER:" + identity.getName() + " SAVING asset [" + path.getFileName() + "]");
        
        assetEditedEvent.fire(new AssetEditedEvent(path));
        return ioService.newOutputStream(paths.convert(path), makeCommentedOption("uploaded"));
    }
    
    public OutputStream save( final Path path,
                              final String comment ) {
        log.info( "USER:" + identity.getName() + " SAVING asset [" + path.getFileName() + "]" );

        assetEditedEvent.fire( new AssetEditedEvent( path ) );   
        return ioService.newOutputStream( paths.convert( path ),
                                          makeCommentedOption( comment ) );
    }

    @Override
    public void delete( final Path path,
                        final String comment ) {
        log.info( "USER:" + identity.getName() + " DELETING asset [" + path.getFileName() + "]" );
        ioService.delete( paths.convert( path ) );
        
        assetEditedEvent.fire( new AssetEditedEvent( path ) );   
    }

    @Override
    public Path rename( final Path path,
                        final String newName,
                        final String comment ) {
        log.info( "USER:" + identity.getName() + " RENAMING asset [" + path.getFileName() + "] to [" + newName + "]" );

        String targetName = path.getFileName().substring( 0, path.getFileName().lastIndexOf( "/" ) + 1 ) + newName;
        String targetURI = path.toURI().substring( 0, path.toURI().lastIndexOf( "/" ) + 1 ) + newName;
        Path targetPath = PathFactory.newPath( path.getFileSystem(), targetName, targetURI );
        ioService.move( paths.convert( path ), paths.convert( targetPath ), new CommentedOption( identity.getName(), comment ) );
        
        assetEditedEvent.fire( new AssetEditedEvent( path ) );   
        return targetPath;
    }

    @Override
    public Path copy( final Path path,
                      final String newName,
                      final String comment ) {
        log.info( "USER:" + identity.getName() + " COPYING asset [" + path.getFileName() + "] to [" + newName + "]" );
        String targetName = path.getFileName().substring( 0, path.getFileName().lastIndexOf( "/" ) + 1 ) + newName;
        String targetURI = path.toURI().substring( 0, path.toURI().lastIndexOf( "/" ) + 1 ) + newName;
        Path targetPath = PathFactory.newPath( path.getFileSystem(), targetName, targetURI );
        ioService.copy( paths.convert( path ), paths.convert( targetPath ), new CommentedOption( identity.getName(), comment ) );
        
        assetEditedEvent.fire( new AssetEditedEvent( path ) );   
        return targetPath;
    }

    private CommentedOption makeCommentedOption( final String commitMessage ) {
        final String name = identity.getName();
        final Date when = new Date();
        final CommentedOption co = new CommentedOption( name,
                                                        null,
                                                        commitMessage,
                                                        when );
        return co;
    }

    @Override
    public void save(Path path, String content, Metadata metadata,
            String comment) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void save(Path path, String content, String comment) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Path save(Path context, String fileName, String content,
            String comment) {
        // TODO Auto-generated method stub
        return null;
    }
}
