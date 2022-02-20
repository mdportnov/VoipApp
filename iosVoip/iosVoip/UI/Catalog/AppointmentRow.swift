import SwiftUI
import Kingfisher
import shared

struct AppointmentRow: View {
    var appointment: Appointment
    var client = KtorClientBuilder()
    var photoUrl: String

    @Environment(\.colorScheme) var colorScheme

    @EnvironmentObject private var viewRouter: ViewRouter
    @EnvironmentObject private var state: AppState

    init(appointment: Appointment) {
        self.appointment = appointment
        if (appointment.line == nil || appointment.line?.isEmpty == true) {
            photoUrl = client.PHOTO_REQUEST_URL_BY_GUID + appointment.EmpGUID!
        } else {
            photoUrl = client.PHOTO_REQUEST_URL_BY_PHONE + appointment.line!
        }
    }

    @State
    var infoRotation = 0.0
    @State
    var infoColor: Color = Color.white
    @State
    var infoIsVisible = false

    var fontSize = 13.0

    var body: some View {
        VStack(alignment: .leading) {
            HStack(alignment: .top, spacing: 5) {
                KFImage(URL(string: photoUrl)!)
                        .resizable()
                        .placeholder {
                            Image(systemName: "person").foregroundColor(.gray)
                        }
                        .fade(duration: 0.4)
                        .cacheOriginalImage()
                        .scaledToFit()
                        .frame(width: 70)
                        .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 5) {
                    Text(appointment.fullName).font(.system(size: fontSize)).bold().scaledToFill().lineLimit(2)

                    if appointment.appointment != nil {
                        Text("Должность:").bold().font(.system(size: fontSize))
                        Text(appointment.appointment!).lineLimit(4).foregroundColor(.gray).font(.system(size: fontSize))
                    }

                    HStack(alignment: .center) {
                        if (appointment.lineShown != nil && appointment.lineShown?.isEmpty == false) {
                            Group {
                                Image(systemName: "phone").foregroundColor(.green)
                                Text("SIP: ").bold().font(.system(size: fontSize))
                                Text("\(appointment.lineShown ?? "")").font(.system(size: fontSize))
                            }
                                    .onTapGesture {
                                        if let line = appointment.lineShown {
                                            viewRouter.open(.caller)
                                            state.inputLine = line
                                            state.isNumPadVisible = true
                                        }
                                    }
                        }
                        if (appointment.lineShown != nil || appointment.room != nil) {
                            Button {
                                infoIsVisible.toggle()
                            } label: {
                                Image(systemName: "chevron.right.circle")
                                        .rotationEffect(.degrees(infoIsVisible ? 90 : 0))
                                        .animation(.spring(), value: infoIsVisible)
                            }
                        }
                    }
                }
                Spacer()
            }
                    .frame(minWidth: 0, maxWidth: .infinity)

            if infoIsVisible {
                VStack(alignment: .leading, spacing: 5) {
                    if (appointment.lineShown != nil && appointment.lineShown?.isEmpty == false) {
                        HStack {
                            Image(systemName: "phone").foregroundColor(.blue)
                            Text("Звонок через телефон").bold()
                                    .font(.system(size: fontSize))
                        }
                                .onTapGesture {
                                    guard let url = URL(string: "tel://+74957885699,\(appointment.lineShown!)"),
                                          UIApplication.shared.canOpenURL(url) else {
                                        return
                                    }
                                    if #available(iOS 10, *) {
                                        UIApplication.shared.open(url)
                                    } else {
                                        UIApplication.shared.openURL(url)
                                    }
                                }
                    }

                    if (appointment.email != nil && appointment.email?.isEmpty == false) {
                        HStack {
                            Image(systemName: "envelope").foregroundColor(.red)
                            Text("Email: ").bold()
                                    .font(.system(size: fontSize))
                            Text("\(appointment.email!)")
                                    .font(.system(size: fontSize))

                        }
                                .onTapGesture {
                                    let email = appointment.email!
                                    if let url = URL(string: "mailto:\(email)") {
                                        if #available(iOS 10.0, *) {
                                            UIApplication.shared.open(url)
                                        } else {
                                            UIApplication.shared.openURL(url)
                                        }
                                    }
                                }
                    }
                    if (appointment.room != nil && appointment.room?.isEmpty == false) {
                        HStack {
                            Image(systemName: "globe").foregroundColor(.purple)
                            Text("Помещение: ").bold().font(.system(size: fontSize))
                            Text("\(appointment.room!)").font(.system(size: fontSize))
                        }
                    }
                }
            }
        }
                .onDisappear {
                    if infoIsVisible == true {
                        changeInfoIcon()
                    }
                }
    }

    func changeInfoIcon() {
        infoIsVisible.toggle()
        if (infoRotation == 0.0) {
            infoRotation = 180.0
            infoColor = .gray
        } else {
            infoRotation = 0.0
            infoColor = (colorScheme == .dark) ? .white : .black
        }
    }
}

struct AppointmentRow_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            AppointmentRow(appointment: Appointment(appointmentId: "1", subscriberId: "1", EmpGUID: "5854", appointment: "Начальник отдела", lastname: "Романов", firstname: "Николай", patronymic: "", fullName: "Романов Николай Николаевич", fio: "Романов Н.Н.", line: "9295", lineShown: "9295", email: "nnromanov@mephi.ru", room: "В-103", positions: nil))
                    .previewInterfaceOrientation(.portrait)
        }
    }
}
