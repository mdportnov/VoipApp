import SwiftUI
import shared
import Combine

extension CallerViewModel : ObservableObject{}

struct CallerScreen: View {
    @ObservedObject var viewModel: CallerViewModel
    
    @State private var isNumPadVisible = false
    
    @State private var inputLine = ""
    
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
                        .overlay(Text(inputLine).background(colorScheme == .dark ? .black : .white))
                        .padding()
                        .background(colorScheme == .dark ? .black : .white)
                        .cornerRadius(30)
                        .shadow(color: .black.opacity(0.3), radius: 2)
                        .frame(height: 80)
                    KeyPad(string: $inputLine)
                }
                .offset(y: self.isNumPadVisible ? 0 : UIScreen.main.bounds.height)
                .animation(.interactiveSpring(response: 0.3, dampingFraction: 0.86,
                                              blendDuration: 0.25), value: isNumPadVisible)
                
                HStack{
                    Spacer()
                    HStack{
                        if isNumPadVisible {
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
                            if !isNumPadVisible {
                                changeNumPadState()
                            } else {
                                tryToCall(sipName: nil)
                            }
                        }){
                            Image(systemName: self.isNumPadVisible == true ? "phone"  : "teletype.answer").resizable()
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
        isNumPadVisible = !isNumPadVisible
    }
    
    func tryToCall(sipName: String?){
        if(inputLine.count > 3){
            viewModel.addRecord(sipNumber: inputLine, sipName: sipName, status: CallStatus.outcoming)
            updateCalls()
            inputLine = ""
        }
        // else TODO
    }
}

extension CallRecord : Identifiable{}

//struct CallerScreen_Previews: PreviewProvider {
//    let vm = DependenciesIosKt.createCallerVM()
//    static var previews: some View {
//        CallerScreen(viewModel: vm)
//    }
//}
