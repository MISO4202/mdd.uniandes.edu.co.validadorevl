package mdd.uniandes.edu.co.validadorevl.handlers;

import java.util.UUID;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.emf.validation.EvlValidator;
import org.eclipse.epsilon.evl.emf.validation.ValidationResults;
import org.eclipse.epsilon.evl.execute.UnsatisfiedConstraint;
import org.eclipse.epsilon.evl.execute.context.IEvlContext;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ValidationHandler extends AbstractHandler {
	
	private static final String CONSOLE_NAME = "EVL Validations";
	private final String modelsUI = UUID.randomUUID().toString()+"-"+UUID.randomUUID().toString();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.saveAllEditors(true);
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		
		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof IResource)
        {
        	IResource resourceTarget = (IResource)firstElement;
        	
        	System.out.println(""+resourceTarget.getFullPath());
        	
        	EvlModule evlModule = new EvlModule();
        	
        	URL resource = getClass().getResource("validations.evl");        	
        	try {
        		System.out.println(resource.getPath());
        		evlModule.parse(resource.toURI());
        		evlModule.getContext().getModelRepository().addModel(createSource(resourceTarget));
        		evlModule.execute();
	        	
        		
        		
	        	IEvlContext context = evlModule.getContext();
	    		List<UnsatisfiedConstraint> uContraints = context.getUnsatisfiedConstraints();
	    		
	    		
	    		MessageConsole myConsole = findConsole(CONSOLE_NAME);
	    		MessageConsoleStream out = myConsole.newMessageStream();
	    		
	    		myConsole.clearConsole();
	    		
    			String id = IConsoleConstants.ID_CONSOLE_VIEW;
    			IConsoleView view = (IConsoleView) page.showView(id);
    			view.display(myConsole);
	    		
	    		
	    		for (UnsatisfiedConstraint evlUnsatisfiedConstraint : uContraints) {	    			
	    			out.println(evlUnsatisfiedConstraint.getMessage());
	    		}
	    	
	    		
	    		
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	
        }
		return null;
	}
	protected EmfModel createSource(IResource resourceTarget) {
		
		
        EmfModel emfModel = new EmfModel();
        emfModel.setName("mydsl");
       
        emfModel.setMetamodelUri("http://www.xtext.org/example/mydsl/MyDsl");
        
        System.out.println(resourceTarget.getLocationURI().getPath());
        
        emfModel.setModelFile(resourceTarget.getLocationURI().getPath());
        emfModel.setReadOnLoad(true);
        emfModel.setStoredOnDisposal(false);
        try {
                emfModel.load();
        } catch (EolModelLoadingException e) {
                e.printStackTrace();
        }
        return emfModel;
}
	private MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	 }
}
