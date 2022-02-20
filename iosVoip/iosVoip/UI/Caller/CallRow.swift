import SwiftUI
import shared
import Kingfisher

struct CallRow: View {
    var callRecord: CallRecord
    var viewModel: CallerViewModel?

    init(callRecord: CallRecord, viewModel: CallerViewModel?) {
        self.callRecord = callRecord
        self.viewModel = viewModel
    }

    var body: some View {
        HStack(alignment: .center) {
            KFImage(URL(string: "https://sd.mephi.ru/api/6/get_photo_mobile.jpg?api_key=theiTh0Wohtho$uquie)kooc&phone=" + callRecord.sipNumber)!)
                .resizable()
                .placeholder{
                    Image(systemName: "person").foregroundColor(.gray)
                }
                .fade(duration: 0.4)
                .cacheMemoryOnly()
                .scaledToFit()
                .frame(width: 70)
                .clipShape(RoundedRectangle(cornerRadius: 30))
            Spacer()
            VStack(alignment: .leading) {
                if callRecord.sipName != nil {
                    Text(callRecord.sipName!).bold()
                }

                HStack(alignment: .center) {
                    Image(systemName: "phone.down").foregroundColor(.red)
                    Text(stringFromDate(Date(timeIntervalSince1970: TimeInterval(callRecord.time)))).font(.system(size: 10)).multilineTextAlignment(.leading).lineLimit(2)
                }
            }
            Text(callRecord.sipNumber).font(.system(size: 20)).bold()
            Button(action: {
                //                viewModel?.tryToCall(sipNumber: callRecord.sipNumber)
            }) {
                Image(systemName: "phone").resizable().foregroundColor(.green).frame(width: 20, height: 20)
            }
        }.frame(maxWidth: .infinity, maxHeight: 60)
    }
}

func stringFromDate(_ date: Date) -> String {
    let formatter = DateFormatter()
    formatter.dateFormat = "dd MMM yyyy HH:mm"
    return formatter.string(from: date)
}

struct CallRow_Previews: PreviewProvider {
    static var previews: some View {
        CallRow(callRecord: CallRecord(id: 1, sipNumber: "8877", sipName: "Труттце А.А.", status: CallStatus.incoming, time: 1643057784), viewModel: nil)
    }
}
