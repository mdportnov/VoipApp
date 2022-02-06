import SwiftUI
import shared
import Combine
import Toaster

extension CallerViewModel : ObservableObject{}

struct CallerScreen: View {
    @ObservedObject var viewModel: CallerViewModel
    
    @EnvironmentObject private var state: AppState
    
    @Environment(\.colorScheme) var colorScheme
    
    @State private var calls : [CallRecord] = []
    
    init(viewModel: CallerViewModel){
        self.viewModel = viewModel
        updateCalls()
    }
    
    func updateCalls(){
        self.calls = viewModel.getAllCallRecords()
    }
    
    func deleteItems(at offsets: IndexSet){
        print("Deletion position: .\(offsets.first!)")
        viewModel.deleteRecord(record: viewModel.callHistory.executeAsList()[offsets.first!])
        updateCalls()
    }
    
    @State private var refreshingID = UUID()
    
    var body: some View {
        ZStack {
            List {
                ForEach(viewModel.callHistory.executeAsList(), id: \.id){
                    item in
                    CallRow(callRecord: item, viewModel: viewModel)
                }.onDelete { IndexSet in
                    deleteItems(at: IndexSet)
                    refreshingID = UUID()
                }.id(refreshingID)
            }
            VStack {
                Spacer()
                VStack{
                    Color.clear
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.accentColor))
                        .overlay(Text(state.inputLine).background(colorScheme == .dark ? .black : .white))
                        .padding()
                        .background(colorScheme == .dark ? .black : .white)
                        .cornerRadius(30)
                        .shadow(color: .black.opacity(0.3), radius: 2)
                        .frame(height: 80)
                    KeyPad(string: $state.inputLine)
                }
                .offset(y: state.isNumPadVisible ? 0 : UIScreen.main.bounds.height)
                .animation(.interactiveSpring(response: 0.3, dampingFraction: 0.86,
                                              blendDuration: 0.25), value: state.isNumPadVisible )
                
                HStack{
                    Spacer()
                    HStack{
                        if state.isNumPadVisible  {
                            Button(action: {
                                changeNumPadState()
                            }){
                                Image(systemName: "chevron.down").resizable()
                                    .frame(width: 30, height: 10)
                                    .padding(20)
                                    .foregroundColor(.white)
                            }.frame(width: 60, height: 60)
                                .background(.orange)
                                .cornerRadius(38.5)
                                .padding()
                                .shadow(color: Color.black.opacity(0.3),
                                        radius: 3, x: 3, y: 3)
                        }
                        
                        Button(action: {
                            if !state.isNumPadVisible  {
                                changeNumPadState()
                            } else {
                                tryToCall(sipName: nil)
                            }
                        }){
                            Image(systemName: state.isNumPadVisible  == true ? "phone"  : "teletype.answer").resizable()
                                .padding(20)
                                .foregroundColor(.white)
                        }.frame(width: 60, height: 60)
                            .background(.green)
                            .cornerRadius(38.5)
                            .padding()
                            .shadow(color: Color.black.opacity(0.3),
                                    radius: 3, x: 3, y: 3)
                    }
                }
            }
        }
    }
    
    func changeNumPadState(){
        state.isNumPadVisible.toggle()
    }
    
    func tryToCall(sipName: String?){
        if(state.inputLine.count > 3){
            viewModel.addRecord(sipNumber: state.inputLine, sipName: sipName, status: CallStatus.outcoming)
            updateCalls()
            state.inputLine = ""
            changeNumPadState()
        }
        else{
            Toast(text: "Введите номер длиннее", duration: Delay.long).show()
        }
        
    }
}

extension CallRecord : Identifiable{}

struct CallerScreen_Previews: PreviewProvider {
    static var previews: some View {
        CallerScreen(viewModel: .init())
    }
}
