import SwiftUI
import shared
import PhotosUI

@main
struct DungeonsAndDeigoApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    @State private var selectedCharacter: Character? = nil
    @State private var showForm = false
    @State private var characterName = ""
    @State private var selectedIndex = 0
    @State private var characters: [Character] = []
    @State private var selectedPhoto: PhotosPickerItem? = nil
    @State private var imageBase64: String? = nil

    private let models = SheetModelsKt.availableSheetModels
    private let repo = IosCharacterRepository()

    var body: some View {
        if let character = selectedCharacter {
            CharacterDetailView(character: character) {
                selectedCharacter = nil
            }
        } else {
            listScreen
        }
    }

    private var listScreen: some View {
        VStack(spacing: 16) {
            Text("Dungeons And Deigo").font(.title)

            if characters.isEmpty {
                Text("No characters created yet.")
            } else {
                ForEach(characters.indices, id: \.self) { i in
                    Button(action: { selectedCharacter = characters[i] }) {
                        HStack {
                            if let base64 = characters[i].imageBase64,
                               let data = Data(base64Encoded: base64.replacingOccurrences(of: "data:image/png;base64,", with: "")),
                               let uiImage = UIImage(data: data) {
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .frame(width: 40, height: 40)
                                    .clipShape(RoundedRectangle(cornerRadius: 4))
                            }
                            Text("\(characters[i].name) (\(characters[i].sheetModelName))")
                        }
                    }
                    .buttonStyle(.plain)
                }
            }

            Button("Create a new Character") { showForm = true }

            if showForm {
                TextField("Character Name", text: $characterName)
                    .textFieldStyle(.roundedBorder)

                Picker("Character Model", selection: $selectedIndex) {
                    ForEach(0..<models.count, id: \.self) { i in
                        Text((models[i] as! SheetModels).name).tag(i)
                    }
                }

                PhotosPicker("Choose Image", selection: $selectedPhoto, matching: .images)
                    .onChange(of: selectedPhoto) { newItem in
                        Task {
                            if let data = try? await newItem?.loadTransferable(type: Data.self) {
                                imageBase64 = "data:image/png;base64," + data.base64EncodedString()
                            }
                        }
                    }

                Button("Submit") {
                    let model = models[selectedIndex] as! SheetModels
                    let character = Character(name: characterName, sheetModelName: model.name, imageBase64: imageBase64)
                    repo.insert(character: character)
                    characterName = ""
                    imageBase64 = nil
                    selectedPhoto = nil
                    showForm = false
                    selectedCharacter = character
                }
            }
        }
        .padding()
        .onAppear { loadCharacters() }
    }

    private func loadCharacters() {
        characters = repo.getAll().compactMap { $0 as? Character }
    }
}

struct CharacterDetailView: View {
    let character: Character
    let onBack: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                if let base64 = character.imageBase64,
                   let data = Data(base64Encoded: base64.replacingOccurrences(of: "data:image/png;base64,", with: "")),
                   let uiImage = UIImage(data: data) {
                    Image(uiImage: uiImage)
                        .resizable()
                        .frame(width: 80, height: 80)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                VStack(alignment: .leading) {
                    Text(character.name).font(.title)
                    Text("Sheet: \(character.sheetModelName)").font(.subheadline)
                }
            }

            Button("Back to selection", action: onBack)

            Spacer()
        }
        .padding()
    }
}
